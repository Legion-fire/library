package com.example.library.controller;

import com.example.library.dto.AuthorResponse;
import com.example.library.dto.BookResponse;

import com.example.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(BookController.class)
@Import(SpringDataWebAutoConfiguration.class)
public class BookPaginationWebTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean
    BookService bookService;
    @Autowired
    RequestMappingHandlerMapping mappings;

    private List<BookResponse> generateBooks() {
        List<BookResponse> list = new ArrayList<>();
        // 12 книг с названиями A..L
        IntStream.rangeClosed(0, 11).forEach(i -> {
            char title = (char) ('A' + i);
            String isbn = "978-5-00000-000" + Math.random();
            LocalDate date = LocalDate.now();
            AuthorResponse authorResponse = new AuthorResponse(((long) (i + 1)), "Михаил", "Янаров");
            list.add(new BookResponse((long) (i + 1), String.valueOf(title), isbn, date, authorResponse));
        });
        return list;
    }
    @Test
    void dumpMappings() {
        mappings.getHandlerMethods().forEach((info, method) ->
                System.out.println(info + " -> " + method));
    }


    @Test
    void shouldMapPageableFromRequestAndReturnCorrectPageMetadata() throws Exception {
        // Дано: всего 12 книг (A. L), нужна страница 1 (вторая), размер 5, сортировка по title ASC
        List<BookResponse> all = generateBooks();
        List<BookResponse> pageContent = all.subList(5, 10); // F..J

        Page<BookResponse> page = new PageImpl<>(
                pageContent,
                PageRequest.of(1, 5, Sort.by(Sort.Order.asc("title"))),
                all.size()
        );


        when(bookService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/books")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "title,asc")).andDo(print()).andExpect(status().isOk())

                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].title").value("F"))
                .andExpect(jsonPath("$.content[4].title").value("J"));
    }
}