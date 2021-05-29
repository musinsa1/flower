package flower;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

 @RestController
 public class ProductController {

 @Autowired
 ProductRepository productRepository;

@RequestMapping(value = "/checkAndModifyStock",
        method = RequestMethod.GET,
        produces = "application/json;charset=UTF-8")

public boolean checkAndModifyStock(@RequestParam("productId") Long productId,
                                @RequestParam("qty") int qty)
        throws Exception {
                boolean status = false;
                Optional<Product> productOptional = productRepository.findByProductId(productId);
                Product product = productOptional.get();

                if(product.getStock() >= qty) {
                        product.setStock(product.getStock() - qty);
                        status = true;

                        productRepository.save(product);
                }

                return status;
        }

 }
