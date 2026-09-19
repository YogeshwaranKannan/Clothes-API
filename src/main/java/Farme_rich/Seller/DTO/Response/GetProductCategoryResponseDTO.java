package Farme_rich.Seller.DTO.Response;

import org.bson.types.ObjectId;

import java.util.List;

public class GetProductCategoryResponseDTO {

    public ObjectId category_Id;
    public String category_name;
    public boolean displayOnHomePage;
    public List<GetProductCategoryResponseDTO> children;

}
