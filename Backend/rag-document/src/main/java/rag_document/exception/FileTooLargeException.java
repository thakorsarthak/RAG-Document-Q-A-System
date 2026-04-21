package rag_document.exception;

public class FileTooLargeException extends  RuntimeException{

    public  FileTooLargeException (long size , long maxSize){

        super(String.format("File size %d bytes exceeds maximum allowed size of %d bytes", size, maxSize));
    }
}
