package LearningConcepts;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
// This is an example file of how paging will be applied on the main project.
public class DatabasePageExample {
    private static final int PAGE_SIZE = 4096;

    public static void main(String[] args) throws Exception {
        // the tuple data
        String name = "akash";
        String address = "kolkata";
        int age = 23;
        String gender = "male";

        // Create database file
        File dbFile = new File("mydb.db");
        if (dbFile.exists()) dbFile.delete();

        try (RandomAccessFile file = new RandomAccessFile(dbFile, "rw");
             FileChannel channel = file.getChannel()) {

            // Create a buffer for the page
            ByteBuffer page = ByteBuffer.allocate(PAGE_SIZE);

            // Before we start we need to understand the ByteBuffer-how its works
            // Now, lets go with the internals of the byteBuffer, byteBuffer itself maintains a pointer called position
            // Here in the below example I am reading putInt() -> this basically means read 4Bytes of Binary data and convert it into Integer.
            //After reading the position of the pointer will be increased by 4Bytes.
            //To understand more about ByteBuffer you can read the PDF provided in the GIT repo.
            // 1. Write page header
            page.putInt(1);          // Page ID = 1
            page.putInt(12);         // Free space pointer starts after header
            page.putInt(0);          // Initially 0 records

            // 2. Encode record : This function is actually creating a tuple with all the metadata will be required.
            ByteBuffer recordBuffer = encodeRecord(name, address, age, gender);
            int recordSize = recordBuffer.limit();

            System.out.println("Record size: " + recordSize);

            // 3. Calculate record position (from end of page)
            int recordPosition = PAGE_SIZE - recordSize;

            // 4. Copy record to page
            recordBuffer.position(0);
            page.position(recordPosition);
            page.put(recordBuffer);

            // 5. Add slot to slot array
            page.position(12);  // Position after header
            page.putInt(recordPosition);

            // 6. Update record count
            page.putInt(8, 1);  // Set record count to 1

            // 7. Write page to disk
            page.position(0);
            channel.write(page);

            System.out.println("Wrote page to disk successfully");

            // 8. Read back and verify
            readPage(dbFile);
        }
    }

    private static ByteBuffer encodeRecord(String name, String address, int age, String gender) {
        byte[] nameBytes = name.getBytes();
        byte[] addressBytes = address.getBytes();
        byte[] genderBytes = gender.getBytes();

        int totalLength = 2 + 2 +                           // Record header
                (1 + 4 + nameBytes.length) +       // Name field
                (1 + 4 + addressBytes.length) +    // Address field
                (1 + 4) +                          // Age field
                (1 + 4 + genderBytes.length);      // Gender field

        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        for(int i=0;i<nameBytes.length;i++){
            System.out.print(nameBytes[i]+" ");
        } // printing what byte array actually holds
        System.out.println();
        // Record header
        buffer.putShort((short)totalLength);
        buffer.putShort((short)4);  // 4 fields

        // Name field
        buffer.put((byte)1);  // Type = String
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);

        // Address field
        buffer.put((byte)1);  // Type = String
        buffer.putInt(addressBytes.length);
        buffer.put(addressBytes);

        // Age field
        buffer.put((byte)2);  // Type = Integer
        buffer.putInt(age);

        // Gender field
        buffer.put((byte)1);  // Type = String
        buffer.putInt(genderBytes.length);
        buffer.put(genderBytes);

        buffer.flip();
        return buffer;
    }

    private static void readPage(File dbFile) throws Exception {
        try (RandomAccessFile file = new RandomAccessFile(dbFile, "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer page = ByteBuffer.allocate(PAGE_SIZE);
            channel.read(page);
            page.flip();

            // Read header
            int pageId = page.getInt();
            int freeSpacePtr = page.getInt();
            int numRecords = page.getInt();

            System.out.println("Page ID: " + pageId);
            System.out.println("Free space pointer: " + freeSpacePtr);
            System.out.println("Number of records: " + numRecords);

            // Read slot array
            for (int i = 0; i < numRecords; i++) {
                int recordOffset = page.getInt();
                System.out.println("Record " + (i+1) + " offset: " + recordOffset);

                // Save position to return to slot array
                int currentPos = page.position();

                // Jump to record
                page.position(recordOffset);

                // Read record header
                short recordLength = page.getShort();
                short fieldCount = page.getShort();

                System.out.println("  Record length: " + recordLength);
                System.out.println("  Field count: " + fieldCount);

                // Read fields
                for (int j = 0; j < fieldCount; j++) {
                    byte fieldType = page.get();

                    if (fieldType == 1) {  // String
                        int strLength = page.getInt();
                        byte[] strBytes = new byte[strLength];
                        page.get(strBytes);
                        String value = new String(strBytes);
                        System.out.println("  Field " + (j+1) + ": String = \"" + value + "\"");
                    }
                    else if (fieldType == 2) {  // Integer
                        int value = page.getInt();
                        System.out.println("  Field " + (j+1) + ": Integer = " + value);
                    }
                }

                // Return to slot array
                page.position(currentPos);
            }
        }
    }
}