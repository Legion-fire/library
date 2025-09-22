package com.example.library.book;

import com.example.library.author.Author;
import com.example.library.author.AuthorRepository;
import com.example.library.book.dto.AuthorResponse;
import com.example.library.book.dto.BookRequest;
import com.example.library.book.dto.BookResponse;
import com.example.library.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    public Page<BookResponse> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book id=" + id + " not found"));
        return toResponse(book);
    }

    public BookResponse create(BookRequest request) {
        Author author = authorRepository.findById(request.authorId())
                .orElseThrow(() -> new NotFoundException("Author id=" + request.authorId() + " not found"));

        Book book = Book.builder()
                .title(request.title())
                .isbn(request.isbn())
                .publishedDate(request.publishedDate())
                .author(author)
                .build();

        Book saved = bookRepository.save(book);
        return toResponse(saved);
    }

    public BookResponse update(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book id=" + id + " not found"));

        if (!book.getAuthor().getId().equals(request.authorId())) {
            Author author = authorRepository.findById(request.authorId())
                    .orElseThrow(() -> new NotFoundException("Author id=" + request.authorId() + " not found"));
            book.setAuthor(author);
        }

        book.setTitle(request.title()); book.setIsbn(request.isbn());
        book.setPublishedDate(request.publishedDate());

        return toResponse(book); // благодаря @Transactional изменения будут сохранены
    }

    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new NotFoundException("Book id=" + id + " not found");
        }
        bookRepository.deleteById(id);
    }

    private BookResponse toResponse(Book book) {
        Author a = book.getAuthor();
        AuthorResponse authorDto = new AuthorResponse(a.getId(), a.getFirstName(), a.getLastName());
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getPublishedDate(),
                authorDto
        );
    }
}
