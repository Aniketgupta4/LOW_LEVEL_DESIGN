package TOMATO;


import java.util.*;
// ===================== MODELS ==========================

class MenuItem {
    String code;
    String name;
    int price;

    public MenuItem(String code, String name, int price) {
        this.code = code;
        this.name = name;
        this.price = price;
    }
}

class Restaurant {
    int id;
    String name;
    String location;
    List<MenuItem> menu;

    public Restaurant(int id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.menu = new ArrayList<>();
    }

    public void addMenuItem(MenuItem item) {
        menu.add(item);
    }

    public List<MenuItem> getMenu() {
        return menu;
    }
}

class Cart {
    Restaurant restaurant;
    List<MenuItem> items = new ArrayList<>();

    public Cart(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void addToCart(MenuItem item) {
        items.add(item);
    }

    public int totalCost() {
        int sum = 0;
        for (MenuItem m : items) sum += m.price;
        return sum;
    }
}

class User {
    int id;
    String name;
    String address;
    Cart cart;

    public User(int id, String name, String address, Restaurant r) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.cart = new Cart(r);
    }
}

// =================== PAYMENT STRATEGY ===================

interface IPaymentStrategy {
    void pay(int amount);
}

class UPI implements IPaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid ₹" + amount + " using UPI");
    }
}

class CreditCard implements IPaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid ₹" + amount + " using Credit Card");
    }
}

class NetBanking implements IPaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid ₹" + amount + " using NetBanking");
    }
}

// ======================= ORDER ==========================

class Order {
    int id;
    User user;
    Restaurant restaurant;
    List<MenuItem> items;
    IPaymentStrategy paymentMethod;

    public Order(int id, User user, Restaurant restaurant, List<MenuItem> items) {
        this.id = id;
        this.user = user;
        this.restaurant = restaurant;
        this.items = items;
    }

    public void setPayment(IPaymentStrategy strategy) {
        this.paymentMethod = strategy;
    }

    public void payBill() {
        int amt = 0;
        for (MenuItem m : items) amt += m.price;
        paymentMethod.pay(amt);
    }

    public String getType() {
        return "NORMAL";
    }
}

class DeliveryOrder extends Order {
    String address;
    public DeliveryOrder(int id, User user, Restaurant restaurant, List<MenuItem> items, String address) {
        super(id, user, restaurant, items);
        this.address = address;
    }

    @Override
    public String getType() {
        return "DELIVERY";
    }
}

class PickupOrder extends Order {
    String counter;
    public PickupOrder(int id, User user, Restaurant restaurant, List<MenuItem> items, String counter) {
        super(id, user, restaurant, items);
        this.counter = counter;
    }

    @Override
    public String getType() {
        return "PICKUP";
    }
}

// ======================= FACTORY =========================

interface IOrderFactory {
    Order createOrder();
}

class NormalOrderFactory implements IOrderFactory {
    User user;
    int id;

    public NormalOrderFactory(User user, int id) {
        this.user = user;
        this.id = id;
    }

    public Order createOrder() {
        return new Order(id, user, user.cart.restaurant, user.cart.items);
    }
}

class ScheduledOrderFactory implements IOrderFactory {
    User user;
    int id;
    String time;

    public ScheduledOrderFactory(User user, int id, String time) {
        this.user = user;
        this.id = id;
        this.time = time;
    }

    public Order createOrder() {
        System.out.println("Order scheduled at " + time);
        return new Order(id, user, user.cart.restaurant, user.cart.items);
    }
}

// ==================== MANAGERS ==========================

class RestaurantManager {
    private static RestaurantManager instance;
    List<Restaurant> restaurants = new ArrayList<>();

    private RestaurantManager() {}

    public static RestaurantManager getInstance() {
        if (instance == null) instance = new RestaurantManager();
        return instance;
    }

    public void addRestaurant(Restaurant r) {
        restaurants.add(r);
    }
}

class OrderManager {
    private static OrderManager instance;
    List<Order> orders = new ArrayList<>();

    private OrderManager() {}

    public static OrderManager getInstance() {
        if (instance == null) instance = new OrderManager();
        return instance;
    }

    public void addOrder(Order order) {
        orders.add(order);
    }
}

// ================== NOTIFICATION SERVICE ===================

class NotificationService {
    public void notifyUser(Order order) {
        System.out.println("Order confirmed! Type: " + order.getType());
    }
}

// ======================= MAIN APP ==========================

public class TOMATO {
    public static void main(String[] args) {

        // 1. Restaurant setup
        Restaurant r = new Restaurant(1, "Biryani Palace", "Mumbai");
        r.addMenuItem(new MenuItem("BIR001", "Chicken Biryani", 250));
        r.addMenuItem(new MenuItem("BIR002", "Mutton Biryani", 350));

        RestaurantManager.getInstance().addRestaurant(r);

        // 2. User
        User user = new User(101, "Aniket", "Jabalpur", r);

        // 3. Add item to cart
        user.cart.addToCart(r.getMenu().get(0));  // add Chicken Biryani

        // 4. Order using Factory
        IOrderFactory factory = new NormalOrderFactory(user, 1);
        Order order = factory.createOrder();

        // 5. Payment method
        order.setPayment(new UPI());

        // 6. Add order
        OrderManager.getInstance().addOrder(order);

        // 7. Pay bill
        order.payBill();

        // 8. Notify
        new NotificationService().notifyUser(order);
    }
}

