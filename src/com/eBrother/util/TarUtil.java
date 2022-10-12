package com.eBrother.util;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;

public class TarUtil {  

	static final int BUFFER_MAX_SIZE = 2048;
	
	public static boolean makeTar ( String sztarget, String szsrc ) {
		
		boolean bret = false;
		OutputStream tar_output = null;
		ArchiveOutputStream my_tar_ball;
		
		try {
			
			tar_output = new FileOutputStream(new File( sztarget ));
			my_tar_ball = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, tar_output);
			
			
			
		}
		catch ( Exception e ) {
			
			
		}
				
				
		return bret;
		
	}
	
	
	public static void main(String[] args) throws Exception{
			/* Read TAR File into TarArchiveInputStream */
		String file = "D:\\eBrotherProject\\rnd\\project\\ebriefing\\inbound\\201405211020.aeis1_125.60.61.72_112640.tar";
		
		TarArchiveInputStream myTarFile=new TarArchiveInputStream(new FileInputStream(new File( file )));
		/* To read individual TAR file */
		TarArchiveEntry entry = null;
		String individualFiles;
		int offset;
		long filesize;
		FileOutputStream outputFile=null;
		BufferedOutputStream outputStream = null;
		
		/* Create a loop to read every single entry in TAR file */
		while ((entry = myTarFile.getNextTarEntry()) != null) {
			/* Get the name of the file */
			individualFiles = entry.getName();
			
			if ( entry.isDirectory()) continue;
			/* Get Size of the file and create a byte array for the size */
			// byte[] content = new byte[(int) entry.getSize()];
			byte[] buffer = new byte[BUFFER_MAX_SIZE];

			offset=0;
			filesize = entry.getSize();

            File tempFile = File.createTempFile(
            		entry.getName(), "");

            outputStream = new BufferedOutputStream(
                    new FileOutputStream(tempFile), BUFFER_MAX_SIZE);

			/* Some SOP statements to check progress */
			System.out.println("File Name in TAR File is: " + entry.isDirectory() + " - " + individualFiles);
			System.out.println("Size of the File is: " + filesize );		  
			
			int count = 0;
			int nread = 0;
			int nrealread = 0;
			while ( nread <= filesize ) {
					  
				count = (int)filesize - nread;
				if ( count >= BUFFER_MAX_SIZE ) count = BUFFER_MAX_SIZE;
				if ( count == 0 ) break;
				System.out.println("Size of the File is: " + nread + " - " + count );	
				nrealread = myTarFile.read(buffer, 0, count);
				outputStream.write(buffer, 0, nrealread);
				nread += nrealread;
			}
			outputStream.close();
		}
		myTarFile.close();
	}
}

