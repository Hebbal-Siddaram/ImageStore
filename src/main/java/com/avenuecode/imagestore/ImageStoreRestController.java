package com.avenuecode.imagestore;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.avenuecode.imagestore.entities.Image;
import com.avenuecode.imagestore.entities.ImageRepository;
import com.avenuecode.imagestore.entities.Product;
import com.avenuecode.imagestore.entities.ProductRepository;

/*
 * Endpoints:
 * POST /{parentId}/product - adds a product as a child of a parent product identified by {productId}. 
 * The special, '-1' product ID, must be used as the parendID for the root product.
 * POST /{parentId}/image - adds an image, as a child of a parent product. * 
 * GET/PUT /product/{productId} - retrieves/updates a product by its id
 * GET/PUT /image/{imageId} - retrieves/updates an image by its id
 */

@RestController
@RequestMapping("/")
class ImageStoreRestController {

	private final ImageRepository imageRepository;

	private final ProductRepository productRepository;

	@Autowired
	ImageStoreRestController(ImageRepository imageRepository,
						   ProductRepository productRepository, MappingJackson2HttpMessageConverter converter) {
		this.imageRepository = imageRepository;
		this.productRepository = productRepository;

	}

//	@RequestMapping(method = RequestMethod.GET)
//	Collection<Image> readBookmarks(@PathVariable String userId) {
//		this.validateProduct(userId);
//		return this.imageRepository.findByProductName(userId);
//	}

	@RequestMapping(method = RequestMethod.POST, value = "{parentId}/image")
	ResponseEntity<?> addImage(@PathVariable Long parentId, @RequestBody Image input) {		
		System.out.println(input);

		return this.productRepository
				.findById(parentId)
				.map(parentProduct -> {
					Image result = imageRepository.save(new Image(parentProduct, input.getDescription()));

					URI location = ServletUriComponentsBuilder
						.fromCurrentRequest().path("/{id}")
						.buildAndExpand(result.getId()).toUri();

					return ResponseEntity.created(location).build();
				})
				.orElseThrow(() -> new ProductNotFoundException(parentId));
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "{parentId}/product")
	ResponseEntity<?> addProduct(@PathVariable Long parentId, @RequestBody Product input) {		

		return this.productRepository
				.findById(parentId)
				.map(parentProduct -> {
					Product result = productRepository.save(new Product(parentProduct,input.getName()));

					URI location = ServletUriComponentsBuilder
						.fromCurrentRequest().replacePath("/product/{id}")
						.buildAndExpand(result.getId()).toUri();

					return ResponseEntity.created(location).build();
				})
				.orElseThrow(() -> new ProductNotFoundException(parentId));
	}	
	
	@RequestMapping(method = RequestMethod.PUT, value = "/product/{productId}")
	ResponseEntity<?> updateProduct(@PathVariable Long productId, @RequestBody Product input) {
		
		Product product = productRepository.findById(productId).orElseThrow(() -> PNFE(productId));
		Product parent;
		if (input.getParentProduct().getId() != product.getParentProduct().getId()) {
			Long otherParentId = input.getParentProduct().getId();
			parent = productRepository.findById(otherParentId).orElseThrow(() -> PNFE(otherParentId));
		} else {
			parent = product.getParentProduct();
		}
		
		productRepository.save(new Product(parent,input.getName()));	
		
		return ResponseEntity.ok().build();

	}
	

	@RequestMapping(method = RequestMethod.GET, value = "product/{productId}")
	Product getProduct(@PathVariable Long productId) {
		return this.productRepository.findById(productId).orElseThrow(() -> PNFE(productId));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "image/{imageId}")
	Image getImage(@PathVariable Long imageId) {
		return this.imageRepository.findById(imageId).orElseThrow(() -> INFE(imageId));
	}
	
	private ImageNotFoundException INFE(Long imageId) {
		throw new ImageNotFoundException (imageId);
	}

	private ProductNotFoundException PNFE(Long productId) {
		throw new ProductNotFoundException(productId);
	}
	

}