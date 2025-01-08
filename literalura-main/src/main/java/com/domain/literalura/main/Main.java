package com.domain.literalura.main;

import com.domain.literalura.model.*;
import com.domain.literalura.repository.*;
import com.domain.literalura.service.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Main {
    private final Scanner keyboard = new Scanner(System.in);
    private final ApiConsulter apiConsulter = new ApiConsulter();
    private final DataConverter dataConverter = new DataConverter();
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public Main(BookRepository bookRepository, AuthorRepository authorRepository) { this.bookRepository = bookRepository; this.authorRepository = authorRepository; }

    public void start() {
        var option = -1;

        while (option != 0) {
            var menu = """
                    \n
                    ======================================
                                  LiterAlura
                    ======================================
                    \n
                    --- Selecione sua opção ---
                    
                    1 - Buscar por título
                    2 - Listar de Livros registrados
                    3 - Listar de autores registrados
                    4 - Listar autores vivos em um determinado ano
                    5 - Listar Livros em determinado idioma
                    6 - Listar Livros top 10 mais baixados
                    7 - Visualizar dados estatisticos dos livros registrados
                                        
                    0 - Sair
                    """;

            System.out.println(menu);

            if (keyboard.hasNextInt()) {
                option = keyboard.nextInt();
                keyboard.nextLine();

                switch (option) {
                    case 1:
                        searchBookByTitle();
                        break;
                    case 2:
                        listRegisteredBooks();
                        break;
                    case 3:
                        listRegisteredAuthors();
                        break;
                    case 4:
                        ListAuthorsAliveInAGivenYear();
                        break;
                    case 5:
                        listBooksByLanguage();
                        break;
                    case 6:
                        listTop10();
                        break;
                    case 7:
                        showDbStatistics();
                        break;
                    case 0:
                        System.out.println("\nfechando a aplicação...");
                        break;
                    default:
                        System.out.println("\nOpção Inválida");
                }

            } else {
                System.out.println("\nEntrada Inválida");
                keyboard.next();
            }
        }
    }

    @Transactional
    private void searchBookByTitle() {
        String BASE_URL = "https://gutendex.com/books/?search=";
        System.out.println("\nDigite o título do livro que deseja buscar: ");
        var title = keyboard.nextLine();

        if (!title.isBlank() && !isANumber(title)) {

            var json = apiConsulter.obtainData(BASE_URL + title.replace(" ", "%20"));
            var data = dataConverter.obtainData(json, Data.class);
            Optional<BookData> searchBook = data.results()
                    .stream()
                    .filter(b -> b.title().toLowerCase().contains(title.toLowerCase()))
                    .findFirst();

            if (searchBook.isPresent()) {
                BookData bookData = searchBook.get();

                if (!verifiedBookExistence(bookData)) {
                    Book book = new Book(bookData);
                    AuthorData authorData = bookData.author().get(0);
                    Optional<Author> optionalAuthor = authorRepository.findByName(authorData.name());

                    if (optionalAuthor.isPresent()) {
                        Author existingAuthor = optionalAuthor.get();
                        book.setAuthor(existingAuthor);
                        existingAuthor.getBooks().add(book);
                        authorRepository.save(existingAuthor);
                    } else {
                        Author newAuthor = new Author(authorData);
                        book.setAuthor(newAuthor);
                        newAuthor.getBooks().add(book);
                        authorRepository.save(newAuthor);
                    }

                    bookRepository.save(book);

                } else {
                    System.out.println("\nLivro adicionado no DB");
                }

            } else {
                System.out.println("\nLivro não existe");
            }

        } else {
            System.out.println("\nEntrada Inválida");
        }

    }

    private void listRegisteredBooks() {
        List<Book> books = bookRepository.findAll();

        if(!books.isEmpty()) {
            System.out.println("\n----- Livro registrado -----");
            books.forEach(System.out::println);
        } else {
            System.out.println("\nNada registrado");
        }

    }

    private void listRegisteredAuthors() {
        List<Author> authors = authorRepository.findAll();

        if(!authors.isEmpty()) {
            System.out.println("\n----- Autor Registrado -----");
            authors.forEach(System.out::println);
        } else {
            System.out.println("\nNada registrado");
        }

    }

    private boolean verifiedBookExistence(BookData bookData) {
        Book book = new Book(bookData);
        return bookRepository.verifiedBDExistence(book.getTitle());
    }

    private void ListAuthorsAliveInAGivenYear() {
        System.out.println("\nDigite o ano que voce quer consultar: ");

        if (keyboard.hasNextInt()) {
            var year = keyboard.nextInt();
            List<Author> authors = authorRepository.findAuthorsAlive(year);

            if (!authors.isEmpty()) {
                System.out.println("\n----- Registrado autor vivo em: " + year + " -----");
                authors.forEach(System.out::println);
            } else {
                System.out.println("\nSem resultados, Digite outro ano: ");
            }

        } else {
            System.out.println("\nEntrada Inválida");
            keyboard.next();
        }

    }

    private void listBooksByLanguage() {
        var option = -1;
        String language = "";

        System.out.println("\nSelecione o idioma para buscar: ");
        var languagesMenu = """
               \n
               1 - Inglês
               2 - Françes
               3 - Alemão
               4 - Português
               5 - Espanhol
               """;

        System.out.println(languagesMenu);

        if (keyboard.hasNextInt()) {
            option = keyboard.nextInt();

            switch (option) {
                case 1:
                    language = "en";
                    break;
                case 2:
                    language = "fr";
                    break;
                case 3:
                    language = "de";
                    break;
                case 4:
                    language = "pt";
                    break;
                case 5:
                    language = "es";
                    break;
                default:
                    System.out.println("\nOpção Inválida");
            }

            System.out.println("\nLivros Registrados:");
            List<Book> books = bookRepository.findBooksByLanguage(language);

            if (!books.isEmpty()) {
                books.forEach(System.out::println);
            } else {
                System.out.println("\nNenhum resultado, selecione outro idioma: ");
            }

        } else {
            System.out.println("\nEntrada Inválida");
            keyboard.next();
        }

    }

    private boolean isANumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void listTop10() {
        List<Book> books = bookRepository.findTop10();

        if (!books.isEmpty()) {
            System.out.println("\n----- Top 10 Livros Baixados -----");
            books.forEach(b -> System.out.println(b.getTitle()));
        } else {
            System.out.println("\nNada aqui !");
        }

    }

    private void showDbStatistics() {
        List<Book> books = bookRepository.findAll();

        if (!books.isEmpty()) {
            IntSummaryStatistics sta = books.stream()
                    .filter(b -> b.getDownloads() > 0)
                    .collect(Collectors.summarizingInt(Book::getDownloads));

            System.out.println("\n----- Dados estatísticos -----");
            System.out.println("Média de downloads: " + sta.getAverage());
            System.out.println("Max downloads: " + sta.getMax());
            System.out.println("Min downloads: " + sta.getMin());
            System.out.println("Livros registrados: " + sta.getCount());
        } else {
            System.out.println("\nNada aqui!");
        }

    }

}
