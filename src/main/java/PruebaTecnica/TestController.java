package PruebaTecnica;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/contacts")
public class TestController {
    private static FileWriter fileWriter;
    @Value("${json.location}")
    private String jsonLocation;
    @GetMapping("/generate")
    public ResponseEntity<Object> generate() throws IOException {
        List<Contact> contacts = new ArrayList<>();
        Faker faker = new Faker();

        for(int i=0;i<10;i++){
            Contact contactModel = new Contact();
            List<String> addresses = new ArrayList<>();

            contactModel.id = faker.number().randomDigit();
            contactModel.name = (String.valueOf(faker.name().firstName()));
            contactModel.phone = (String.valueOf(faker.phoneNumber().cellPhone()));

            for(int n=0;n<3;n++) {
                addresses.add(faker.address().fullAddress());
            }

            contactModel.addressLine = (addresses);

            contacts.add(contactModel);

        }

        File file = ResourceUtils.getFile(jsonLocation);

        String path = System.getProperty("user.dir");
        log.info(path + jsonLocation);
        fileWriter = new FileWriter(path + jsonLocation);
        fileWriter.write(new Gson().toJson(contacts));

        fileWriter.flush();
        fileWriter.close();

        return ResponseEntity.ok().
                header("Content-Type", "application/json")
                .body(contacts);
    }

    @DeleteMapping("/delete/{contact_id}")
    public ResponseEntity<Object> deleteContact (@PathVariable int contact_id) throws IOException {
        String path = System.getProperty("user.dir");
        log.info("Location:"+ path + jsonLocation);
        List<Contact> contactsList = new ArrayList<>();
        List<Contact> contactsFiltred = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        contactsList = Arrays.asList(mapper.readValue(new String(Files.readAllBytes(Paths.get(path + jsonLocation))), Contact[].class));

        Predicate<Contact> filter = contact -> contact.id == contact_id;

        contactsFiltred = contactsList.stream().filter(filter).toList();

        if (!contactsFiltred.isEmpty()){

            List<Contact> newContactList = new ArrayList<>();

            for (Contact contact: contactsList){
                if(!contactsFiltred.contains(contact))
                    newContactList.add(contact);
            }

            File file = ResourceUtils.getFile(jsonLocation);
            log.info(path + jsonLocation);
            fileWriter = new FileWriter(path + jsonLocation);
            fileWriter.write(new Gson().toJson(newContactList));

            fileWriter.flush();
            fileWriter.close();
        }else{
            return ResponseEntity.notFound()
                    .header("Content-Type", "application/json").build();
        }

        return (ResponseEntity<Object>) ResponseEntity.noContent().
                header("Content-Type", "application/json").build();
    }
    @GetMapping("/get/{contact_id}")
    public ResponseEntity<Object> getContact(@PathVariable int contact_id) throws IOException {
        String path = System.getProperty("user.dir");
        log.info("Location:"+ path + jsonLocation);
        List<Contact> contactsList = new ArrayList<>();
        List<Contact> contactsFiltred = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        contactsList = Arrays.asList(mapper.readValue(new String(Files.readAllBytes(Paths.get(path + jsonLocation))), Contact[].class));

        Predicate<Contact> filter = contact -> contact.id == contact_id

        contactsFiltred = contactsList.stream().filter(filter).toList()

        if (contactsFiltred.isEmpty()){
            return ResponseEntity.notFound()
                    .header("Content-Type", "application/json").build()
        }

        return ResponseEntity.ok().
                header("Content-Type", "application/json")
                .body(contactsFiltred);
    }

    @GetMapping("/getList/{phrase}")
    public ResponseEntity<Object> getList (@PathVariable(required = false) String phrase) throws IOException {
        String path = System.getProperty("user.dir");
        log.info("Location:"+ path + jsonLocation);
        ObjectMapper mapper = new ObjectMapper();
        List<Contact> contactsList = new ArrayList<>();
        List<Contact> contactsFiltred = new ArrayList<>();
        contactsList = Arrays.asList(mapper.readValue(new String(Files.readAllBytes(Paths.get(path + jsonLocation))), Contact[].class));

        if (!phrase.isEmpty()){
            log.info(phrase.toString().toLowerCase());

            contactsFiltred = filter(contactsList, phrase);

            contactsFiltred.forEach(c -> log.info(c.name));

            Collections.sort(contactsFiltred, new Comparator<Contact>() {
                @Override
                public int compare(Contact a, Contact b) {
                    return a.name.compareTo(b.name);
                }
            } );
        }else{
            return ResponseEntity.badRequest()
                    .header("Content-Type", "application/json").build();
        }

        return ResponseEntity.ok().
                header("Content-Type", "application/json")
                .body(contactsFiltred);
    }


    private static List<Contact> filter(List<Contact> lista, String phrase) {

        Set<Character> letrasPermitidas = new HashSet<>();

        for(int i=0;i<phrase.length();i++) {
            char letra = phrase.toLowerCase().charAt(i);
            letrasPermitidas.add(letra);
        };

        StringBuilder regexBuilder = new StringBuilder(".*[");

        for (char letra : letrasPermitidas) {
            regexBuilder.append(Pattern.quote(String.valueOf(letra)));
        }

        regexBuilder.append("].*");
        String regex = regexBuilder.toString();
        Pattern pattern = Pattern.compile(regex);

        List<Contact> modelFiltered = new ArrayList<>();
        for (Contact model : lista) {
            Matcher matcher = pattern.matcher(model.name.toLowerCase());
            if(!matcher.find()){
                modelFiltered.add(model);
            }
        }

        return modelFiltered;
    }
}
