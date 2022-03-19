package Server.Data;

import Server.Model.Client;
import Server.PasswordHasher;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DataProvider {

    public static List<Client> listClients = new LinkedList<>();

    static {
        //Passwords stored in hashes
        DataProvider.listClients.add(new Client("Lukman", PasswordHasher.toHash("Lukman","123456"))); //123456
        DataProvider.listClients.add(new Client("Yibing", PasswordHasher.toHash("Yibing","123456Yc"))); //123456Yc
        DataProvider.listClients.add(new Client("John", PasswordHasher.toHash("John","123456J"))); //123456J
        DataProvider.listClients.add(new Client("Doe", PasswordHasher.toHash("Doe","123456D"))); //123456D
    }

}
