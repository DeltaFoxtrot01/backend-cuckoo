package com.cuckoo.BackendServer.models.contactTracing;

import lombok.SneakyThrows;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import lombok.Getter;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

/** Cuckoo Filter implementation
 *  Constants calculated for:
 *      - an expected false positive rate of 0.002;
 *      - an expected load factor of 0.95;
 *      - an expected number of entries of 42.
 *
 *  The optimal size for a bucket should be:
 *      - 4 if false positive rate between 0.0001 and 0.002;
 *      - 2 if false positive rate greater than 0.002.
 *
 *  The load factor (0.50, 0.84, 0.95, 0.98) increases with the bucket size (1, 2, 4, 8).
 *
 *  The optimal number of bits for the fingerprint can be calculated by:
 *      f = ceil(log(2 * bucketSize / false positive rate) / log(2))
 *  There are 2^f possible fingerprints.
 *
 *  The optimal number of buckets can be computed as:
 *      ceil((number of entries / load factor) / bucket size)
 *  Note that this may need to be a prime number to avoid collisions.
 *  Since only the fingerprints will be sent through the network and not the whole table,
 *      we use more buckets than the optimal number to reduce collisions.
 *
 *  We will also consider a maximum number of kicks for each collision to avoid infinite loops.
 */
public class CuckooFilter {

    private static final int MAX_NUM_BUCKETS = 31; // optimal is 12
    private static final int MAX_BUCKET_SIZE = 4;
    private static final int MAX_NUM_FINGERPRINTS = (int) Math.pow(2, 12);
    private static final int MAX_NUM_KICKS = 10;

    private final int[][] table = new int[MAX_NUM_BUCKETS][MAX_BUCKET_SIZE];

    private static final int MAX_BYTE_VAL = 256;
    private static final int MAX_PRIME = 4099; // first prime number greater than MAX_NUM_FINGERPRINTS

    @Getter
    private int count;

    public CuckooFilter() {
        clear();
    }

    public boolean insert(byte[] patientHash) {
        int fp = getFingerprint(patientHash);
        int b1 = tableHash(patientHash);
        int b2 = xorHash(b1, fp);

        int bucketWithSlots = -1;
        int emptySlot = -1;
        for (int i = 0; i < MAX_BUCKET_SIZE; i++) {
            if (table[b1][i] == fp || table[b2][i] == fp) {
                // Fingerprint already in filter
                return true;
            } else if (table[b1][i] == -1) {
                bucketWithSlots = b1;
                emptySlot = i;
            } else if (table[b2][i] == -1) {
                bucketWithSlots = b2;
                emptySlot = i;
            }
        }

        this.count++;
        if (bucketWithSlots == -1) {
            return kickOutEntry(b1, b2, fp);
        }

        table[bucketWithSlots][emptySlot] = fp;
        return true;
    }
    
    private boolean kickOutEntry(int b1, int b2, int fp) {
        int b = new Random().nextBoolean() ? b1 : b2;
        for (int k = 0; k < MAX_NUM_KICKS; k++) {
            int i = new Random().nextInt(MAX_BUCKET_SIZE);
            int kickedFp = table[b][i];
            table[b][i] = fp;

            fp = kickedFp;
            b = xorHash(b, kickedFp);
            for (i = 0; i < MAX_BUCKET_SIZE; i++) {
                if (table[b][i] == -1) {
                    table[b][i] = fp;
                    return true;
                }
            }
        }

        this.count--;
        return false;
    }

    public boolean isPresent(byte[] patientHash) {
        int b1 = tableHash(patientHash);
        int fp = getFingerprint(patientHash);
        int b2 = xorHash(b1, fp);

        for (int i = 0; i < MAX_BUCKET_SIZE; i++)
            if (table[b1][i] == fp || table[b2][i] == fp)
                return true;
        
        return false;
    }

    /**
     *  Computes the Rabin fingerprint for a given byte array.
     *  Each byte is singled out and the hash is computed as follows:
     *      h = (b1 * base^0 + b2 * base^1 + ... + bn * base^(n-1)) % MAX_NUM_FINGERPRINTS,
     *      where:
     *          - bn is the nth byte;
     *          - n is the total of bytes;
     *          - base is greater than the largest possible byte value.
     *
     *  Since n can reach high values, exponentiation can result in overflow.
     *  We use the following property to avoid this problem:
     *      (a*b) % n = (a%n * b%n) % n
     */
    private int getFingerprint(byte[] patientHash) {
        long fp = 0;
        long exp = 0;

        for (long b : patientHash) {
            // Optimized exponentiation
            long p = b & 0xff; // Necessary for hashes to be compatible with frontend
            for (long e = 0; e < exp; e++)
                p = (p * MAX_BYTE_VAL) % MAX_PRIME;

            fp = (fp + p) % MAX_PRIME;
            exp++;
        }

        return Math.floorMod(fp, MAX_NUM_FINGERPRINTS);
    }

    /**
     *  Algorithm used for sdbm database library.
     *  The actual function is h(i) = h(i-1) * 65599 + b[i].
     *  This implementation is a faster computation of said function.
     *  65599 is a prime number and was obtained after experimenting with different constants.
     */
    private int tableHash(byte[] patientHash) {
        long h = 0;
        for (long b : patientHash)
            h = (b & 0xff) + (h << 6) + (h << 16) - h;
        return Math.floorMod(h, MAX_NUM_BUCKETS);
    }

    private int xorHash(int otherBucketHash, int fingerprint) {
        byte[] bytes = ByteBuffer.allocate(8).putLong(fingerprint).array();
        return (otherBucketHash ^ tableHash(bytes)) % MAX_NUM_BUCKETS;
    }

    public void clear() {
        for (int b = 0; b < MAX_NUM_BUCKETS; b++)
            for (int i = 0; i < MAX_BUCKET_SIZE; i++)
                table[b][i] = -1;

        this.count = 0;
    }

    @SneakyThrows
    @Override
    public String toString() {
        JSONArray entries = new JSONArray();

        for (int b = 0; b < MAX_NUM_BUCKETS; b++)
            for (int i = 0; i < MAX_BUCKET_SIZE; i++)
                if (table[b][i] > -1) {
                    JSONObject json = new JSONObject();
                    json.put("fingerprint", table[b][i]);
                    json.put("bucket", b);
                    json.put("pos", i);
                    entries.put(json);
                }

        return entries.toString();
    }
}
