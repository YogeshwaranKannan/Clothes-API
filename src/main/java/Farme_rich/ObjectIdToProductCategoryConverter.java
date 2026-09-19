package Farme_rich;

import Farme_rich.Seller.Model.BackEnd.ProductCategory;
import Farme_rich.Seller.Repo.BackEnd.ProductCategoryRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import org.springframework.context.annotation.Lazy;

@Component
@ReadingConverter
public class ObjectIdToProductCategoryConverter implements Converter<ObjectId, ProductCategory> {

    @Autowired
    @Lazy
    private ProductCategoryRepo productCategoryRepo;

    @Override
    public ProductCategory convert(ObjectId source) {
        return productCategoryRepo.findById(source.toString()).orElse(null);
    }
}
