package com.soumen.customer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Repository
interface CustomerRepository extends ReactiveCrudRepository<Customer, Long> {
}

@SpringBootApplication
@Log4j2
@OpenAPIDefinition(info = @Info(title = "Customer API Application Demo", version = "1.0", description = "Customer API Application - Documentation APIs v1.0"))
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }

    @Bean
    ApplicationRunner initDB(CustomerRepository customerRepository, DatabaseClient dbc) {
        return args -> {
            var ddl = dbc
                    .sql("create table customer(id serial primary key, name varchar(255) not null)")
                    .fetch()
                    .rowsUpdated();
            var names = Flux.just("Soumen", "Ian", "Tomas", "Jose", "Max")
                    .map(name -> new Customer(null, name))
                    .flatMap(customerRepository::save);
            var all = customerRepository.findAll();
            ddl.thenMany(names).thenMany(all).subscribe(log::info);
        };
    }


    @Bean
    @RouterOperations(
            {
                    @RouterOperation(path = "/customer", produces = {
                            MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, beanClass = CustomerHandler.class, beanMethod = "getCustomers",
                            operation = @Operation(operationId = "getCusomers", responses = {
                                    @ApiResponse(responseCode = "200", description = "successful operation", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            array = @ArraySchema(schema = @Schema(implementation = Customer.class)))})}
                            )),
                    @RouterOperation(path = "/customer", produces = {
                            MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST, beanClass = CustomerHandler.class, beanMethod = "addCustomer",
                            operation = @Operation(operationId = "addCusomers", responses = {
                                    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Customer.class)))}
                            ))
            })
    public RouterFunction<ServerResponse> customerAPI(CustomerHandler handler) {
        return RouterFunctions
                .route(GET("/customer"), handler::getCustomers)
                .andRoute(POST("/customer"), handler::addCustomer);
    }

}

@Service
record CustomerHandler(CustomerRepository customerRepository) {

    public Mono<ServerResponse> getCustomers(ServerRequest serverRequest) {
        Flux<Customer> all = customerRepository.findAll();
        return ServerResponse.ok().body(all, Customer.class);
    }

    public Mono<ServerResponse> addCustomer(ServerRequest serverRequest) {
        Mono<Customer> customerMono = serverRequest.bodyToMono(Customer.class);
        return customerMono.doOnNext(this.customerRepository::save)
                .map(s -> ServerResponse.ok().body(s, Customer.class)).flatMap(serverResponseMono -> serverResponseMono);

    }
}

record Customer(@Id Long id, String name) {
}




