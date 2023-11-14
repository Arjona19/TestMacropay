package PruebaTecnica;

import lombok.Data;

import java.util.List;

@Data
public class Contact {
    public int id;
    public String name;
    public String phone;
    public List<String> addressLine;
}
