package br.com.apollo.controllers;

import br.com.apollo.models.Book;
import br.com.apollo.proxy.CambioProxy;
import br.com.apollo.repositories.BookRepository;
import br.com.apollo.response.Cambio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("book-service")
public class BookController {

    @Autowired
    private Environment environment;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CambioProxy cambioProxy;

    /*http://localhost:8000/book-service/1/BRL*/
    @GetMapping(value = "/{id}/{currency}")
    public Book findBook(@PathVariable("id") Long id,
                         @PathVariable("currency") String currency) {

        var book = bookRepository.findById(id).get();

        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book Not found");
        }

        var cambio = cambioProxy.getCambio(book.getPrice(), "USD", currency);
        var port = environment.getProperty("local.server.port");
        book.setEnvironment(port + " FEIGN");
        book.setPrice(cambio.getConvertedValue());
        return book;
    }
}