package com.example.CamelRest.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UserRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("timer://fetchUsers?repeatCount=1") // Pour déclencher une seule fois au démarrage
                .routeId("user-api-to-db-route")
                .log("Fetching users from API...")
                .to("http://localhost:8080/api/users") // Appel de l'API REST
                .unmarshal().json(JsonLibrary.Jackson, List.class)
                .split(body())
                .process(exchange -> {
                    Map<String, Object> user = exchange.getIn().getBody(Map.class);
                    String query = String.format(
                            "INSERT INTO users (id, name, email) VALUES (%s, '%s', '%s')",
                            user.get("id"),
                            user.get("name"),
                            user.get("email")
                    );
                    exchange.getIn().setBody(query);
                })
                .to("jdbc:dataSource")
                .end()
                .log("Users inserted into database.");
    }
}
