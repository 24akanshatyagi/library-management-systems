package model;

public class Book {

    private Integer isbn;
    private String title;
    private Integer editionNo;
    private Integer numofCop;
    private Integer numleft;
    private String authors;

    @Override
    public String toString() {
        return "Book Information for " +
                "ISBN : " + isbn +
                "\n\t Title: '" + title + '\'' +
                "\n\t Edition No : " + editionNo +
                "\n\t Number Copies :" + numofCop +
                "\n\t Number Left: " + numleft +
                "\n\t Authors: '" + authors + '\'' +
                '\n';
    }

    public Book(Integer isbn, String title, Integer editionNo, Integer numofCop, Integer numleft, String authors ) {
        this.isbn = isbn;
        this.title = title;
        this.editionNo=editionNo;
        this.numofCop = numofCop;
        this.numleft = numleft;
        this.authors = authors;
    }

}
