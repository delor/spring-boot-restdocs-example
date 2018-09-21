package com.example.restdoc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
public class RestdocApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestdocApplication.class, args);
    }
}

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Greeting.GreetingBuilder.class)
class Greeting {
    Long id;
    @NotBlank
    String message;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class GreetingBuilder {
    }
}

@RestController
@RequestMapping("/greetings")
@AllArgsConstructor
class GreetingControler {

    private GreetingRepository repository;

    @PostMapping
    Greeting create(@Valid @RequestBody Greeting greeting) {
        return repository.save(greeting);
    }
}

interface GreetingRepository {
    Greeting save(Greeting greeting);
}

@Repository
class InMemoryGreetingRepository implements GreetingRepository {

    private AtomicLong idGenerator = new AtomicLong();
    private Map<Long, Greeting> greetings = new HashMap<>();

    @Override
    public Greeting save(Greeting greeting) {
        if (greeting.getId() == null) {
            return createNewGreeting(greeting);
        } else if (greetingExists(greeting)) {
            return updateGreeting(greeting);
        } else {
            throw new RuntimeException("unknown id=" + greeting.getId());
        }
    }

    private Greeting createNewGreeting(Greeting greeting) {
        Long id = idGenerator.incrementAndGet();
        Greeting newGreeting = new Greeting(id, greeting.getMessage());
        greetings.put(newGreeting.getId(), newGreeting);
        return newGreeting;
    }

    private boolean greetingExists(Greeting greeting) {
        return greetings.containsKey(greeting.getId());
    }

    private Greeting updateGreeting(Greeting greeting) {
        Greeting newGreeting = greetings.get(greeting.getId())
                .toBuilder()
                .message(greeting.getMessage())
                .build();
        greetings.put(newGreeting.getId(), newGreeting);
        return newGreeting;
    }
}
