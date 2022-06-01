package org;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        String file = "src/main/resources/1984.txt";
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            Flux<String> lines = Flux.generate(
                    () -> br,
                    (state, sink) -> {
                        try {
                            String line = state.readLine();
                            if (line != null)
                                sink.next(line);
                            else
                                sink.complete();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return br;
                    }
            );
            HashMap<String, AtomicInteger> dict = new HashMap<>();
            lines
                    .flatMap(line -> {
                        return Flux.fromArray(line.split(" "));
                    })
                    .flatMap(word -> Mono.just(word
                            .replaceAll("[^A-Za-zА-Яа-я0-9]", "")
                            .toLowerCase(Locale.ROOT)))
                    .doOnNext(item -> {
                        if (dict.containsKey(item))
                            dict.get(item).incrementAndGet();
                        else
                            dict.put(item, new AtomicInteger(1));
                    })
                    .subscribe();
            dict.entrySet().forEach(entry -> {
                System.out.println(entry.getKey() + " " + entry.getValue());
            });
        }
    }
}
