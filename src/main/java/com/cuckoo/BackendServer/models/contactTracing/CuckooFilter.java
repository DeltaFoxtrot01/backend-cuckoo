package com.cuckoo.BackendServer.models.contactTracing;

import java.util.Objects;
import java.util.Random;

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
    private static final int FINGERPRINT_MASK = (int) Math.pow(2, 12) - 1;
    private static final int MAX_NUM_KICKS = 10;

    private final int[][] table;

    public CuckooFilter() {
        table = new int[MAX_NUM_BUCKETS][MAX_BUCKET_SIZE];
    }

    /**
     *  Computes the Rabin fingerprint for a given byte array.
     *  Each byte is singled out and the hash is computed as follows:
     *      h = (b1 * base^0 + b2 * base^1 + ... + bn * base^(n-1)) % FINGERPRINT_MASK,
     *      where:
     *          - bn is the nth byte;
     *          - n is the total of bytes;
     *          - base is greater than the largest possible byte value.
     *
     *  Since n can reach high values, exponentiation can result in overflow.
     *  We use the following property to avoid this problem:
     *      (a*b) % n = (a%n * b%n) % n
     *
     *  Default value in the table is 0 hence the +1 in the return statement.
     */
    private int getFingerprint(byte[] patientHash) {
        int fp = 0;
        final int base = 256;
        int exp = 0;
        final int mod = 4099; // first prime number greater than FINGERPRINT_MASK

        for (int b : patientHash) {
            // Optimized exponentiation
            int p = b;
            for (int e = 0; e < exp; e++)
                p = (p * base) % mod;

            fp = (fp + p) % mod;
            exp++;
        }

        return 1 + (fp % FINGERPRINT_MASK);
    }

    /**
     *  Algorithm used for sdbm database library.
     *  The actual function is h(i) = h(i-1) * 65599 + b[i].
     *  This implementation is a faster computation of said function.
     *  65599 is a prime number and was obtained after experimenting with different constants.
     */
    private int hash(byte[] patientHash) {
        int h = 0;
        for (int b : patientHash)
            h = b + (h << 6) + (h << 16) - h;

        return h % MAX_NUM_BUCKETS;
    }

    private int xorHash(int otherBucketHash, int fingerprint) {
        return otherBucketHash ^ (Objects.hash(fingerprint) % MAX_NUM_BUCKETS);
    }

    public void insert(byte[] patientHash) {
        int fp = getFingerprint(patientHash);
        int b1 = hash(patientHash);
        int b2 = xorHash(b1, fp);

        int bucketWithSlots = -1;
        int emptySlot = -1;
        for (int i = 0; i < MAX_BUCKET_SIZE; i++) {
            if (table[b1][i] == fp || table[b2][i] == fp) {
                // Fingerprint already in filter
                return;
            } else if (table[b1][i] == 0) {
                bucketWithSlots = b1;
                emptySlot = i;
            } else if (table[b2][i] == 0) {
                bucketWithSlots = b2;
                emptySlot = i;
            }
        }

        if (bucketWithSlots == -1) {
            kickOutEntries(b1, b2, fp);
        } else {
            table[bucketWithSlots][emptySlot] = fp;
        }
    }
    
    private void kickOutEntries(int b1, int b2, int fp) {
        int b = new Random().nextBoolean() ? b1 : b2;
        for (int k = 0; k < MAX_NUM_KICKS; k++) {
            int i = new Random().nextInt(MAX_BUCKET_SIZE);
            int kickedFp = table[b][i];
            table[b][i] = fp;

            fp = kickedFp;
            b = xorHash(b, kickedFp);
            for (i = 0; i < MAX_BUCKET_SIZE; i++) {
                if (table[b][i] == 0) {
                    table[b][i] = fp;
                    return;
                }
            }
        }
    }

    public boolean isPresent(byte[] patientHash) {
        int b1 = hash(patientHash);
        int fp = getFingerprint(patientHash);
        int b2 = xorHash(b1, fp);

        for (int i = 0; i < MAX_BUCKET_SIZE; i++) {
            if (table[b1][i] == fp || table[b2][i] == fp)
                return true;
        }
        
        return false;
    }
}
