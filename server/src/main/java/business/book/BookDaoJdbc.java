package business.book;

import business.BookstoreDbException;
import business.JdbcUtils;
import business.category.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import business.BookstoreDbException.BookstoreQueryDbException;

import javax.xml.transform.Result;

public class BookDaoJdbc implements BookDao {

    private static final String FIND_BY_BOOK_ID_SQL =
            "SELECT book_id, title, author, price, is_public, category_id, rating, is_featured, description " +
                    "FROM book " +
                    "WHERE book_id = ?";

    private static final String FIND_BY_CATEGORY_ID_SQL = "SELECT book_id, title, author, price, is_public, category_id, rating, is_featured, description " +
            "from book " +
            "WHERE category_id = ?";
    // TODO Implement this constant to be used in the findByCategoryId method

    private static final String FIND_RANDOM_BY_CATEGORY_ID_SQL =
            "SELECT book_id, title, author, price, is_public, category_id, rating, is_featured, description " +
                    "FROM book " +
                    "WHERE category_id = ? " +
                    "ORDER BY RAND() " +
                    "LIMIT ?";

    @Override
    public Book findByBookId(long bookId) {
        Book book = null;
        try (Connection connection = JdbcUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_BOOK_ID_SQL)) {
            statement.setLong(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    book = readBook(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new BookstoreQueryDbException("Encountered a problem finding book " + bookId, e);
        }
        return book;
    }

    @Override
    public List<Book> findByCategoryId(long categoryId) {
        List<Book> books = new ArrayList<>();
        try(Connection conn = JdbcUtils.getConnection();
            PreparedStatement statement = conn.prepareStatement(FIND_BY_CATEGORY_ID_SQL)){
                statement.setLong(1,categoryId);
                try(ResultSet resultSet = statement.executeQuery()){
                    while(resultSet.next()){
                        books.add(readBook(resultSet));
                    }
                }
        }
        catch (SQLException e){
            throw new BookstoreQueryDbException("Encountered a problem finding a category " + categoryId);
        }

        return books;
    }

    @Override
    public List<Book> findRandomByCategoryId(long categoryId, int limit) {
        List<Book> books = new ArrayList<>();
        System.out.println("Default limit is "+limit);
        try(Connection conn = JdbcUtils.getConnection();
        PreparedStatement statement = conn.prepareStatement(FIND_RANDOM_BY_CATEGORY_ID_SQL)){
            statement.setLong(1, categoryId);
            statement.setInt(2, limit);
            try(ResultSet res = statement.executeQuery()){
                while(res.next()){
                    books.add(readBook(res));
                }
            }
        }
        catch (SQLException e){
            throw new BookstoreQueryDbException("Encountered a problem finding a category " + categoryId);
        }

        return books;
    }


    private Book readBook(ResultSet resultSet) throws SQLException {
        long bookId = resultSet.getLong("book_id");
        String title = resultSet.getString("title");
        String author = resultSet.getString("author");
        int price = resultSet.getInt("price");
        boolean isPublic = resultSet.getBoolean("is_public");
        long categoryId = resultSet.getLong("category_id");
        boolean isFeatured = resultSet.getBoolean("is_featured");
        int rating = resultSet.getInt("rating");
        String description = resultSet.getString("description");

        return(new Book(bookId, title, author, description, price, rating, isPublic, isFeatured, categoryId));
    }
}
