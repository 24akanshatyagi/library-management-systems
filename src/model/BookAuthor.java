package model;

public class BookAuthor {

    private Integer isbn;
    private Integer authorId;
    private Integer authorseqno;
    private String title;
    private Integer editionNo;
    private Integer numofCop;
    private Integer numleft;
    private String name;
    private String surname;

    public BookAuthor (Integer isbn, Integer authorId, Integer authorseqno, String title,
                      Integer editionNo, Integer numofCop, Integer numleft,
                      String name, String surname) {
        this.isbn = isbn;
        this.authorId = authorId;
        this.authorseqno = authorseqno;
        this.title = title;
        this.editionNo = editionNo;
        this.numofCop = numofCop;
        this.numleft = numleft;
        this.name = name;
        this.surname = surname;
    }

    public BookAuthor() {
    }

    public Integer getIsbn() {
        return this.isbn;
    }

    public void setIsbn(Integer isbn) {
        this.isbn = isbn;
    }

    public Integer getAuthorId() {
        return this.authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getAuthorseqno() {
        return this.authorseqno;
    }

    public void setAuthorseqno(Integer authorseqno) {
        this.authorseqno = authorseqno;
    }
}
