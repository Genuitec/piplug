package russotto.iff;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Stack;

@SuppressWarnings("rawtypes")
public class IFFFile extends RandomAccessFile {
    protected Stack openchunks;

    public IFFFile(String name, String mode) throws IOException {
	super(name, mode);
	openchunks = new Stack();
    }

    public IFFFile(File file, String mode) throws IOException {
	super(file, mode);
	openchunks = new Stack();
    }

    public void chunkSeek(int offset) throws IOException {
	seek(((Long) openchunks.peek()).longValue() + 4 + offset);
    }

    public int getChunkPointer() throws IOException {
	return (int) getFilePointer()
		- (int) ((Long) openchunks.peek()).longValue() - 4;
    }
}
