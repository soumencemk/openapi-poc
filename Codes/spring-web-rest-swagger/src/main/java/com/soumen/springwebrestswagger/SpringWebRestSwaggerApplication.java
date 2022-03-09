package com.soumen.springwebrestswagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
@Log4j2
@OpenAPIDefinition(info = @Info(title = "Customer API", version = "1.0", description = "Customer Information"))
public class SpringWebRestSwaggerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringWebRestSwaggerApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(CustomerRepository repository) {
        return args -> {
            Stream.of("Soumen", "Adam", "Denis", "Manuel", "Basil").map(name -> new Customer(UUID.randomUUID().toString(), name)).forEach(customer -> log.info(repository.save(customer)));
        };
    }

}


@RestController
@RequiredArgsConstructor
class CustomerController {

    private final CustomerRepository customerRepository;

    @Operation(summary = "Get a List of Customers")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Customer.class)))})})
    @GetMapping("/customer")
    public ResponseEntity<List<Customer>> getAllCustomer() {
        return ResponseEntity.ok().body(customerRepository.findAll());
    }

    @Operation(summary = "Save a customer")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Added a Customer", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Customer.class))})})
    @PostMapping("/customer")
    public ResponseEntity<ResponseObject> addCustomer(Customer customer) {
        Customer c = customerRepository.save(customer);
        ResponseObject success = new ResponseObject("Success", Instant.now().toString(), c);
        return ResponseEntity.status(201).body(success);
    }
}


@Repository
class CustomerRepository {

    private List<Customer> customerList = new ArrayList<>();

    public List<Customer> findAll() {
        return customerList;
    }

    public Customer save(Customer customer) {
        customerList.add(customer);
        return customer;
    }
}

record Customer(String id, String name) {
}

record ResponseObject(String status, String timestamp, Customer customer) {
}

