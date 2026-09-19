package Farme_rich.Seller.Repo.BackEnd;


import Farme_rich.Seller.DTO.Request.QuickInvoiceRequest;
import Farme_rich.Seller.Model.BackEnd.QuickInvoice;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuickInvoiceRepo extends MongoRepository<QuickInvoice, ObjectId> {
    QuickInvoice insert(QuickInvoiceRequest quickInvoiceRequest);
    List<QuickInvoice> findByCompanyid(ObjectId companyid);

    Optional<QuickInvoice> findTopByCompanyidOrderByOrderDateDesc(ObjectId companyid);



}
