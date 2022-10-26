package business.order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static business.JdbcUtils.getConnection;
import business.BookstoreDbException;
import business.BookstoreDbException.BookstoreQueryDbException;
import business.BookstoreDbException.BookstoreUpdateDbException;

public class LineItemDaoJdbc implements LineItemDao {

    private static final String CREATE_LINE_ITEM_SQL =
            "INSERT INTO customer_order_line_item (book_id, customer_order_id, quantity) " +
                    "VALUES (?, ?, ?)";

    private static final String FIND_BY_CUSTOMER_ORDER_ID_SQL =
            "SELECT book_id, customer_order_id, quantity " +
                    "FROM customer_order_line_item WHERE customer_order_id = ?";

    @Override
    public void create(Connection connection, long bookId, long orderId, int quantity) {
        try (PreparedStatement statement = connection.prepareStatement(CREATE_LINE_ITEM_SQL)) {
            statement.setLong(1, bookId);
            statement.setLong(2, orderId);
            statement.setInt(3, quantity);
            int affected = statement.executeUpdate();
            if (affected != 1) {
                throw new BookstoreUpdateDbException("Failed to insert an order line item, affected row count = " + affected);
            }

        } catch (SQLException e) {
            System.out.println("Print exception in Line Item Dao Jdbc "+e);
            throw new BookstoreUpdateDbException("Encountered problem creating a new line item ", e);
        }
    }

    @Override
    public List<LineItem> findByOrderId(long orderId) {
        List<LineItem> result = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CUSTOMER_ORDER_ID_SQL)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(readLineItem(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new BookstoreQueryDbException("Encountered problem finding ordered books for customer order "
                    + orderId, e);
        }
        return result;
    }

    private LineItem readLineItem(ResultSet resultSet) throws SQLException {
        long bookId = resultSet.getLong("book_id");
        long orderId = resultSet.getLong("customer_order_id");
        int quantity = resultSet.getInt("quantity");
        return new LineItem(bookId, orderId, quantity);
    }
}
