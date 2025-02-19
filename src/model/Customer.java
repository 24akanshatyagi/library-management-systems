package model;

public class Customer {

    private Integer customerid;
    private String name;
    private String city;
    private String titles;

    public Customer(Integer customerid, String name, String city, String titles) {
        this.customerid = customerid;
        this.name = name;
        this.city = city;
        this.titles = titles;
    }

    @Override
    public String toString() {
        return "Customer Information of ID " +
                ": " + customerid +
                "\n\t Name : '" + name + '\'' +
                "\n\t City : '" + city + '\'' +
                "\n\t Books borrowed : '" + titles + '\'' +
                '\n';
    }

}
