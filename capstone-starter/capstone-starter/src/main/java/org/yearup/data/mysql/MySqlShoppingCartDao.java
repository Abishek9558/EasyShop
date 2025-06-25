package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    private final ProductDao productDao;

    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao)
    {
        super(dataSource);
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId)
    {
        ShoppingCart cart = new ShoppingCart();
        String sql = """
            SELECT p.*, s.quantity 
            FROM shopping_cart s 
            JOIN products p ON s.product_id = p.product_id 
            WHERE s.user_id = ?;
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                Product product = MySqlProductDao.mapRow(rs);
                int quantity = rs.getInt("quantity");

                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(product);
                item.setQuantity(quantity);

                cart.add( item );
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return cart;
    }

    @Override
    public void addItem(int userId, int productId)
    {
        String checkSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";

        try (Connection conn = getConnection())
        {
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, productId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next())
            {
                int currentQty = rs.getInt("quantity");
                String updateSql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, currentQty + 1);
                updateStmt.setInt(2, userId);
                updateStmt.setInt(3, productId);
                updateStmt.executeUpdate();
            }
            else
            {
                String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, productId);
                insertStmt.executeUpdate();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void updateCartItem(int userId, int productId, int quantity)
    {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, quantity);
            stmt.setInt(2, userId);
            stmt.setInt(3, productId);
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
