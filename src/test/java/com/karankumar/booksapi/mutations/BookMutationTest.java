/*
* Copyright (C) 2021  Karan Kumar
*
* This program is free software: you can redistribute it and/or modify it under the terms of the
* GNU General Public License as published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.

* This program is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
* PURPOSE.  See the GNU General Public License for more details.

* You should have received a copy of the GNU General Public License along with this program.
* If not, see <https://www.gnu.org/licenses/>.
*/

package com.karankumar.booksapi.mutations;

import com.acme.DgsConstants;
import com.karankumar.booksapi.exception.InvalidISBN10Exception;
import com.karankumar.booksapi.exception.InvalidISBN13Exception;
import com.karankumar.booksapi.model.Book;
import com.karankumar.booksapi.model.PublishingFormat;
import com.karankumar.booksapi.model.genre.Genre;
import com.karankumar.booksapi.model.genre.GenreName;
import com.karankumar.booksapi.model.language.Lang;
import com.karankumar.booksapi.model.language.LanguageName;
import com.karankumar.booksapi.service.BookService;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.karankumar.booksapi.mutations.BookMutation.NOT_FOUND_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class BookMutationTest {

  @MockBean private BookService bookService;

  @Autowired private BookMutation underTest;

  @MockBean private DataFetchingEnvironment dataFetchingEnvironment;

  private static final Book BOOK_TEMPLATE =
      new Book(
          "Book",
          new Lang(LanguageName.ALBANIAN),
          "Blurb",
          new Genre(GenreName.ADVENTURE),
          new PublishingFormat(PublishingFormat.Format.PAPERBACK));

  @Test
  public void deleteBook_throws_whenIdWasNotFound() {
    // given
    when(dataFetchingEnvironment.getArgument(DgsConstants.BOOK.Id)).thenReturn(null);

    // when/then
    ResponseStatusException exception =
        Assertions.assertThrows(
            ResponseStatusException.class, () -> underTest.deleteBook(dataFetchingEnvironment));

    assertSoftly(
        softly -> {
          softly.assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
          softly.assertThat(exception.getReason()).isEqualTo(NOT_FOUND_ERROR_MESSAGE);
        });
  }

  @Test
  public void deleteBook_throws_whenBookWasNotFound() {
    // given
    when(dataFetchingEnvironment.getArgument(DgsConstants.BOOK.Id)).thenReturn("1");
    when(bookService.findById(1L)).thenReturn(Optional.empty());

    // when/then
    ResponseStatusException exception =
        Assertions.assertThrows(
            ResponseStatusException.class, () -> underTest.deleteBook(dataFetchingEnvironment));

    verify(bookService, times(1)).findById(1L);

    assertSoftly(
        softly -> {
          softly.assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
          softly.assertThat(exception.getReason()).isEqualTo(NOT_FOUND_ERROR_MESSAGE);
        });
  }

  @Test
  public void deleteBook_shouldReturnDeletedBook() {
    // given
    Book book = BOOK_TEMPLATE;

    when(dataFetchingEnvironment.getArgument(DgsConstants.BOOK.Id)).thenReturn("1");
    when(bookService.findById(1L)).thenReturn(Optional.of(book));

    // when
    Book resultBook = underTest.deleteBook(dataFetchingEnvironment);

    // then
    verify(bookService, times(1)).deleteBook(1L);

    assertThat(resultBook).isSameAs(book);
  }

  @Test
  public void addIsbn13_shouldReturnUpdatedBook()
      throws InvalidISBN13Exception, InvalidISBN10Exception {

    // given
    String isbn13 = "978-1-56619-909-4";
    Book book = BOOK_TEMPLATE;

    when(dataFetchingEnvironment.getArgument(DgsConstants.BOOK.Id)).thenReturn(1L);
    when(dataFetchingEnvironment.getArgument(DgsConstants.BOOK.Isbn13)).thenReturn(isbn13);
    when(bookService.findById(1L)).thenReturn(Optional.of(book));
    when(bookService.save(book)).thenReturn(book);

    // when
    Book resultBook = underTest.addIsbn13(dataFetchingEnvironment);

    // then
    assertSoftly(
        softly -> {
          softly.assertThat(resultBook.getIsbn13()).isSameAs(isbn13);
          softly.assertThat(resultBook).isSameAs(book);
        });
  }

  @Test
  public void addIsbn13_throws_whenBookWasNotFound() {
    // given
    when(dataFetchingEnvironment.getArgument(DgsConstants.BOOK.Id)).thenReturn(1L);
    when(bookService.findById(1L)).thenReturn(Optional.empty());

    // when/then
    ResponseStatusException exception =
        Assertions.assertThrows(
            ResponseStatusException.class, () -> underTest.addIsbn13(dataFetchingEnvironment));

    verify(bookService, times(1)).findById(1L);

    assertSoftly(
        softly -> {
          softly.assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
          softly.assertThat(exception.getReason()).isEqualTo(NOT_FOUND_ERROR_MESSAGE);
        });
  }
}
