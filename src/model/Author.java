package model;

public class Author {

    private Integer authorId;
    private String name;

    private String books;

    public Author(Integer authorId, String name, String books) {
        this.authorId = authorId;
        this.name = name;
        this.books = books;
    }

    public Author() {
    }

    public Integer getAuthorId() {
        return this.authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Author Information for Author Id " +
                ": " + authorId +
                "\n\t Name : '" + name + '\'' +
                "\n\t Books : '" + books + '\'' +
                '\n';
    }
}
