package business.order;

import api.ApiException;
import business.BookstoreDbException;
import business.JdbcUtils;
import business.book.Book;
import business.book.BookDao;
import business.cart.ShoppingCart;
import business.cart.ShoppingCartItem;
import business.customer.Customer;
import business.customer.CustomerDao;
import business.customer.CustomerForm;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class DefaultOrderService implements OrderService {

	private BookDao bookDao;
	private OrderDao orderDao;
	private LineItemDao lineItemDao;
	private CustomerDao customerDao;

	public void setOrderDao(OrderDao orderDao) {
		this.orderDao = orderDao;
	}

	public void setLineItemDao(LineItemDao lineItemDao) {
		this.lineItemDao = lineItemDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	public void setBookDao(BookDao bookDao) {
		this.bookDao = bookDao;
	}

	@Override
	public OrderDetails getOrderDetails(long orderId) {
		Order order = orderDao.findByOrderId(orderId);
		Customer customer = customerDao.findByCustomerId(order.getCustomerId());
		List<LineItem> lineItems = lineItemDao.findByOrderId(orderId);
		List<Book> books = lineItems
				.stream()
				.map(lineItem -> bookDao.findByBookId(lineItem.getBookId()))
				.collect(Collectors.toList());
		return new OrderDetails(order, customer, lineItems, books);
	}
	private Date getDate(String monthString, String yearString) {

		int month = Integer.parseInt(monthString);
		int year = Integer.parseInt(yearString);

		LocalDate ld = YearMonth.of(year, month).atEndOfMonth();
		Instant instant = ld.atStartOfDay().toInstant(ZoneOffset.UTC);

		Date date = Date.from(instant);

		return(date);
	}

	private int generateConfirmationNumber(){
		Random random = new Random();
		return(random.nextInt(999999999));
	}
	private long performPlaceOrderTransaction(
			String name, String address, String phone,
			String email, String ccNumber, Date date,
			ShoppingCart cart, Connection connection) {
		try {
			connection.setAutoCommit(false);
			long customerId = customerDao.create(
					connection, name, address, phone, email,
					ccNumber, date);
			long customerOrderId = orderDao.create(
					connection,
					cart.getComputedSubtotal() + cart.getSurcharge(),
					generateConfirmationNumber(), customerId);
			for (ShoppingCartItem item : cart.getItems()) {
				lineItemDao.create(connection, item.getBookId(),
						customerOrderId,item.getQuantity());
			}
			connection.commit();
			return customerOrderId;
		} catch (Exception e) {
			System.out.println("Printing exception "+e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new BookstoreDbException("Failed to roll back transaction", e1);
			}
			return 0;
		}
	}
	@Override
    public long placeOrder(CustomerForm customerForm, ShoppingCart cart) {

		validateCustomer(customerForm);
		validateCart(cart);

		// NOTE: MORE CODE PROVIDED NEXT PROJECT

		try (Connection connection = JdbcUtils.getConnection()) {
			Date date = getDate(
					customerForm.getCcExpiryMonth(),
					customerForm.getCcExpiryYear());
			return performPlaceOrderTransaction(
					customerForm.getName(),
					customerForm.getAddress(),
					customerForm.getPhone(),
					customerForm.getEmail(),
					customerForm.getCcNumber(),
					date, cart, connection);
		} catch (SQLException e) {
			throw new BookstoreDbException("Error during close connection for customer order", e);
		}

	}

	public String cleanString(String phone){

		char[] charArr = phone.toCharArray();
		String fin = "";
		for(char c:charArr){
			if(c == '(' || c == ')' || c == ' ' || c == '-'){
				continue;
			}
			else
				fin += c;
		}

		return(fin);
	}

	public boolean isNumber(String cc){
		for(char c: cc.toCharArray()){
			if(c >= '0' && c <= '9'){
				continue;
			}
			else{
				return(false);
			}
		}
		return(true);
	}

	public boolean isBasicChecksPassed(Object s){
		String stringClass = "".getClass().getName();
		if(s == null || s.getClass().getName() != stringClass || s.equals("")){
			return(false);
		}
		return(true);
	}
	private void validateCustomer(CustomerForm customerForm) {

    	String name = customerForm.getName();
		String address = customerForm.getAddress();
		String phone = this.cleanString(customerForm.getPhone());
		String email = customerForm.getEmail();
		String ccNumber = this.cleanString(customerForm.getCcNumber());

		//Clean the phone number

		String stringClass = "".getClass().getName();

		if (!this.isBasicChecksPassed(name) || name.length() > 45 || name.length() < 4) {
			throw new ApiException.InvalidParameter("Invalid name field");
		}

		if(!this.isBasicChecksPassed(address) || address.length() > 45 || address.length() < 4){
			throw new ApiException.InvalidParameter("Invalid address field");
		}

		if(!this.isBasicChecksPassed(phone) || phone.length() != 10 || !this.isNumber(phone)){
			throw new ApiException.InvalidParameter("Invalid phone field");
		}

		if(!this.isBasicChecksPassed(email)|| email.contains(" ") || (!email.contains("@")) || email.toCharArray()[email.length()-1] == '.' ){
			throw new ApiException.InvalidParameter("Invalid email field");
		}

		if(!this.isBasicChecksPassed(ccNumber) || ccNumber.length()<14 || ccNumber.length()>16 || !this.isNumber(ccNumber)){
			throw new ApiException.InvalidParameter("Invalid ccNumber field");
		}

		if (!this.isBasicChecksPassed(customerForm.getCcExpiryMonth())
				||!this.isBasicChecksPassed(customerForm.getCcExpiryYear())
				|| expiryDateIsInvalid(customerForm.getCcExpiryMonth(), customerForm.getCcExpiryYear())) {
			throw new ApiException.InvalidParameter("Invalid expiry date");

		}
	}

	private boolean expiryDateIsInvalid(String ccExpiryMonth, String ccExpiryYear) {

		// TODO: return true when the provided month/year is before the current month/yeaR
		// HINT: Use Integer.parseInt and the YearMonth class

		YearMonth ym;

		try {
			ym = YearMonth.of(Integer.parseInt(ccExpiryYear), Integer.parseInt(ccExpiryMonth));
		}
		catch(DateTimeException e){
			return(true);
		}

		Calendar calendar = Calendar.getInstance();

		if(ym.isAfter(YearMonth.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1))
		|| ym.equals(YearMonth.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1))){
			return(false);
		}

		return true;

	}

	private void validateCart(ShoppingCart cart) {

		if (cart.getItems().size() <= 0) {
			throw new ApiException.InvalidParameter("Cart is empty.");
		}

		cart.getItems().forEach(item-> {
			if (item.getQuantity() < 0 || item.getQuantity() > 99) {
				throw new ApiException.InvalidParameter("Invalid quantity");
			}
			Book databaseBook = bookDao.findByBookId(item.getBookId());
			// TODO: complete the required validations
			if(item.getBookForm().getCategoryId() != databaseBook.getCategoryId()){
				throw new ApiException.InvalidParameter("Invalid category");
			}

			if(item.getBookForm().getPrice() != databaseBook.getPrice()){
				throw new ApiException.InvalidParameter("Invalid price");
			}
		});
	}

}
