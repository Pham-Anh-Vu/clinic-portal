package com.company.clinicportal.lienthong;

import com.company.clinicportal.lienthong.entity.LienThongDonThuocOutbox;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Spring-proxied bridge để gọi các method transactional từ {@link LienThongOutboxService}
 * — tránh self-call bypass AOP.
 *
 * <p>Vì scheduler chạy bên trong {@code LienThongOutboxService}, gọi {@code this.tick()}
 * hoặc {@code this.pickPending()} từ đó sẽ không đi qua Spring proxy → @Transactional
 * không hoạt động. Bridge này là 1 bean riêng, mỗi lần gọi đều qua proxy.</p>
 */
@Component
public class OutboxWorkerBridge {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private ObjectProvider<LienThongGuiDonThuocService> guiServiceProvider;

    @Transactional
    public void sweepStaleLocks() {
        Instant threshold = Instant.now().minus(Duration.ofMinutes(5));
        List<LienThongDonThuocOutbox> stale = dataManager.load(LienThongDonThuocOutbox.class)
                .query("select e from " + LienThongOutboxService.ENTITY_NAME + " e where e.status = :s "
                        + "and e.lockedAt < :t")
                .parameter("s", LienThongOutboxService.STATUS_IN_PROGRESS)
                .parameter("t", Date.from(threshold))
                .list();
        if (!stale.isEmpty()) {
            for (LienThongDonThuocOutbox row : stale) {
                row.setStatus(LienThongOutboxService.STATUS_PENDING);
                row.setLockedBy(null);
                row.setLockedAt(null);
                row.setLastError((row.getLastError() == null ? "" : row.getLastError() + "\n")
                        + "Sweep at " + Instant.now());
            }
            dataManager.save(new SaveContext().saving(stale.toArray()));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<LienThongDonThuocOutbox> pickPending() {
        Instant now = Instant.now();
        List<LienThongDonThuocOutbox> list = dataManager.load(LienThongDonThuocOutbox.class)
                .query("select e from " + LienThongOutboxService.ENTITY_NAME + " e "
                        + "where e.status in (:pending, :retry) and e.nextAttemptAt <= :now "
                        + "order by e.nextAttemptAt asc")
                .parameter("pending", LienThongOutboxService.STATUS_PENDING)
                .parameter("retry", LienThongOutboxService.STATUS_RETRY)
                .parameter("now", Date.from(now))
                .maxResults(1)
                .list();
        Optional<LienThongDonThuocOutbox> opt = list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        if (opt.isEmpty()) return opt;
        LienThongDonThuocOutbox row = opt.get();
        row.setStatus(LienThongOutboxService.STATUS_IN_PROGRESS);
        row.setAttempt((row.getAttempt() == null ? 0 : row.getAttempt()) + 1);
        row.setLockedBy("worker");
        row.setLockedAt(Date.from(Instant.now()));
        row.setUpdatedAt(Date.from(Instant.now()));
        dataManager.save(new SaveContext().saving(row));
        return opt;
    }

    /**
     * Pipeline tick: sweep stale → pick 1 → process. Được gọi từ
     * {@link LienThongOutboxService#tick()}.
     */
    public void sweepAndPick(LienThongOutboxService owner) {
        sweepStaleLocks();
        Optional<LienThongDonThuocOutbox> row = pickPending();
        row.ifPresent(r -> owner.processJobViaBridge(r, guiServiceProvider.getIfAvailable()));
    }
}
