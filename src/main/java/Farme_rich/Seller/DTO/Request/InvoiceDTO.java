package Farme_rich.Seller.DTO.Request;

/**
 * Response item for GET /api/dashboard/invoices
 * One entry per date for the last 3 days, with a Paid and Unpaid
 * invoice count so the UI can draw two bars per date.
 */
public class InvoiceDTO {

    private String date;      // x-axis
    private long paidCount;   // orders where total_AmountDue == 0
    private long unpaidCount; // orders where total_AmountDue > 0

    public InvoiceDTO() {
    }

    public InvoiceDTO(String date, long paidCount, long unpaidCount) {
        this.date = date;
        this.paidCount = paidCount;
        this.unpaidCount = unpaidCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(long paidCount) {
        this.paidCount = paidCount;
    }

    public long getUnpaidCount() {
        return unpaidCount;
    }

    public void setUnpaidCount(long unpaidCount) {
        this.unpaidCount = unpaidCount;
    }
}
