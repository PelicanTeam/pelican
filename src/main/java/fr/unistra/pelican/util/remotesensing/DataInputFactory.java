package fr.unistra.pelican.util.remotesensing;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.InputStream;

public class DataInputFactory {
	public static DataInput createDataInputBigEndian(InputStream is) {
		return new DataInputStream(is);
	}
	public static DataInput createDataInputLittleEndian(InputStream is) {
		return new LEDataInputStream(is);
	}
}
