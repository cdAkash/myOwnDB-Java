package LearningConcepts;
import java.nio.ByteBuffer;
import java.util.*;

public class HowByteBufferWorks {
    public static void main(String[] args){
        //In this example I am going to create a buffer with some data, this buffer will just allocate the space
        //in physical memory, not storing it in non-volatile disk.
        /*
        1. Now, the ByteBuffer, first of all we have to allocate some space first, we will use .allocate() function.
        2. There are two phases, one is writing and other one is reading
         */
        ByteBuffer buffer = ByteBuffer.allocate(100);
        // Position = 0, Limit = 100, Capacity = 100
        // this allocate() function is defining the buffer inside the JVM, here java will do the garbage collection
        //here in this part it will be all controlled and managed by JVM
//        ByteBuffer buffer2 = ByteBuffer.allocateDirect(100);
        /*
        But this allocateDirect() is allocating the memory directly into physical ram, we have to handle the memory allocation and
        reallocation, or we have to rely on Operating System.
        But in later part I will try to write my own virtual file which will handle the memory allocation part.
        For now, I Will use the allocate()
         */

        // This will be example of ByteBuffer life-cycle
        /*
        Suppose we have a row with some data
         */
        String name="ironman";
        int size = name.length();
        int age = 45;
        // Now, if we want to store this data into disk first we have put that in memory then in disk
        // IN this example just putting the data into memory and reading it again.
        buffer.put(name.getBytes()); // position = 7 ->after every insertion of byte position automatically increases
        //Here getBytes() converting each char to its ascii value and then converting that ascii value into 8-bit binary number
        //and storing it into allocated memory from start.
        buffer.putInt(age); //position = 11 int takes 4 bytes
        buffer.flip();
        //position = 0, limit = 11, capacity = 100
        /*
        Here the position pointer gets 0, and limit=11, now while reading we know starting point and ending point,
        so we don't have to read unnecessarily , we can use the limit pointer to know the empty space left
         */
        //Before reading we have to flip the buffer to set the position to 0.
        // Now reading phase
        byte[] text = new byte[size]; // why byte array for string-> when converting from char to ascii, its store the value into byte so its takes only 1 byte of size;
        buffer.get(text);
        int readAge = buffer.getInt();
        String readName = new String(text); // we parsed the byte array to form a string.
        System.out.println("Name:"+readName);
        System.out.println("Age:"+readAge);

        int totalDataSize=buffer.limit();
        System.out.println(totalDataSize);
        buffer.clear(); //reset for reuse
        // Position = 0, Limit = 100, Capacity = 100 -> we can reuse this memory again and again.


    }
}
