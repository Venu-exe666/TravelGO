package com.travelgo.service;

import com.travelgo.entity.Refund;
import com.travelgo.repository.RefundRepository;
import com.travelgo.entity.Payment;
import com.travelgo.entity.VisaApplication;
import com.travelgo.enums.RefundStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RefundService {

    private final RefundRepository repository;

    public RefundService(RefundRepository repository) {
        this.repository = repository;
    }

    public List<Refund> findAll() {
        return repository.findAll();
    }

    public Optional<Refund> findById(Long id) {
        return repository.findById(id);
    }

    public Refund save(Refund entity) {
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Rule 11: When a visa application is rejected, only the TravelGo documentation handling charge is refunded.
     * The official government/consulate visa fee is non-refundable by law and embassy policy.
     */
    public void simulateVisaRejectionRefund(VisaApplication visa, Payment originalPayment) {
        // Rule 11: Only the documentation charge is refunded. The visa charge is NOT refunded.
        Refund refund = new Refund();
        refund.setPayment(originalPayment);
        refund.setAmount(visa.getDocumentationCharge());
        refund.setReason("Visa Application Rejected. Partial refund (Documentation Charge only).");
        refund.setStatus(RefundStatus.REFUND_PROCESSED);
        refund.setProcessedAt(java.time.LocalDateTime.now());
        
        repository.save(refund);
    }
}
