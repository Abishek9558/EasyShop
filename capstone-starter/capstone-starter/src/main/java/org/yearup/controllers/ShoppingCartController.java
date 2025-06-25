package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@PreAuthorize("isAuthenticated()")
@CrossOrigin
public class ShoppingCartController {

    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProductDao productDao;

    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // GET /cart - Get the shopping cart for the current user
    @GetMapping
    public ShoppingCart getCart(Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            return shoppingCartDao.getByUserId(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch shopping cart.");
        }
    }

    // POST /cart/products/{productId} - Add product to cart (or increment quantity)
    @PostMapping("/products/{productId}")
    public ShoppingCart addProductToCart(@PathVariable int productId, Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            shoppingCartDao.addItem(user.getId(), productId);
            return shoppingCartDao.getByUserId(user.getId()); // add 1 quantity
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add product to cart.");
        }
    }

    // PUT /cart/products/{productId} - Update quantity of an existing product in the cart
    @PutMapping("/products/{productId}")
    public ShoppingCart updateCartItem(@PathVariable int productId, @RequestBody Map<String, Integer> body, Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            int quantity = body.getOrDefault("quantity", 1);

            shoppingCartDao.updateCartItem(userId, productId, quantity);
            return shoppingCartDao.getByUserId(user.getId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update product in cart.");
        }
    }

    // DELETE /cart - Clear all items from current user's cart
    @DeleteMapping
    public ShoppingCart clearCart(Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            shoppingCartDao.clearCart(userId);
            return shoppingCartDao.getByUserId(user.getId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to clear cart.");
        }
    }
}


//package org.yearup.controllers;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.server.ResponseStatusException;
//import org.yearup.data.ProductDao;
//import org.yearup.data.ShoppingCartDao;
//import org.yearup.data.UserDao;
//import org.yearup.models.ShoppingCart;

//import org.yearup.models.User;
//
//import java.security.Principal;
//
//// convert this class to a REST controller
//// only logged in users should have access to these actions
//public class ShoppingCartController
//{
//    // a shopping cart requires
//    private ShoppingCartDao shoppingCartDao;
//    private UserDao userDao;
//    private ProductDao productDao;
//
//
//
//    // each method in this controller requires a Principal object as a parameter
//    public ShoppingCart getCart(Principal principal)
//    {
//        try
//        {
//            // get the currently logged in username
//            String userName = principal.getName();
//            // find database user by userId
//            User user = userDao.getByUserName(userName);
//            int userId = user.getId();
//
//            // use the shoppingcartDao to get all items in the cart and return the cart
//            return null;
//        }
//        catch(Exception e)
//        {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
//        }
//    }
//
//    // add a POST method to add a product to the cart - the url should be
//    // https://localhost:8080/cart/products/15 (15 is the productId to be added
//
//
//    // add a PUT method to update an existing product in the cart - the url should be
//    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
//    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated
//
//
//    // add a DELETE method to clear all products from the current users cart
//    // https://localhost:8080/cart
//
//}
