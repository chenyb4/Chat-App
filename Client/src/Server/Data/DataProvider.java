package Server.Data;

import Server.Model.Client;
import Server.PasswordHasher;

import java.util.ArrayList;
import java.util.List;

public class DataProvider {

    public static List<Client> listClients = new ArrayList<>();

    static {
        listClients.add(new Client("Lukman", PasswordHasher.toHash("123456"))); //123456
        listClients.add(new Client("Yibing", PasswordHasher.toHash("123456Yc"))); //123456Yc
        listClients.add(new Client("John", PasswordHasher.toHash("123456J"))); //123456J
        listClients.add(new Client("Doe", PasswordHasher.toHash("123456D"))); //123456D
    }

}
