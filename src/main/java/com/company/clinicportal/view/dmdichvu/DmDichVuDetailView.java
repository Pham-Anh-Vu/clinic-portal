package com.company.clinicportal.view.dmdichvu;

import com.company.clinicportal.entity.DmDichVu;
import com.company.clinicportal.entity.GiaKpi;
import com.company.clinicportal.enumentity.LoaiGiaKPI;
import com.company.clinicportal.enumentity.TrangThaiDichVu;
import com.company.clinicportal.view.main.MainView;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "dm-dich-vus/:id", layout = MainView.class)
@ViewController(id = "DmDichVu.detail")
@ViewDescriptor(path = "dm-dich-vu-detail-view.xml")
@EditedEntityContainer("dmDichVuDc")
@DialogMode(width = "80%", height = "100%")
public class DmDichVuDetailView extends StandardDetailView<DmDichVu> {
    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private MessageBundle messageBundle;

    @ViewComponent
    private TextField tenDichVuField;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        Map<LoaiGiaKPI, GiaKpi> giaKpiMap = dataManager.load(GiaKpi.class)
                .query("select g from GiaKpi g where g.loai in :loai")
                .parameter("loai", List.of("sang", "toi"))
                .list()
                .stream()
                .collect(Collectors.toMap(GiaKpi::getLoai, Function.identity()));

        GiaKpi giaKpiSang = giaKpiMap.get(LoaiGiaKPI.SANG);
        GiaKpi giaKpiToi = giaKpiMap.get(LoaiGiaKPI.TOI);

        if (giaKpiSang != null) {
            getEditedEntity().setGiaKpiSang(giaKpiSang.getGia());
        }
        if (giaKpiToi != null) {
            getEditedEntity().setGiaKpiToi(giaKpiToi.getGia());
        }

        // Mặc định khi tạo mới dịch vụ: trạng thái = Đang hoạt động.
        DmDichVu edited = getEditedEntity();
        if (edited.getId() == null && edited.getTrangThai() == null) {
            edited.setTrangThai(TrangThaiDichVu.HOAT_DONG);
        }
    }

    /**
     * Validate khi lưu:
     *  - Tên dịch vụ không được trùng với bản ghi đã tồn tại trong database
     *    (so sánh không phân biệt hoa/thường, bỏ qua khoảng trắng đầu/cuối).
     *  - Lỗi hiển thị inline dưới field "tenDichVuField", form không bị lưu.
     */
    @Subscribe
    public void onValidation(final ValidationEvent event) {
        DmDichVu entity = getEditedEntity();

        String tenDichVu = entity.getTenDichVu();
        if (tenDichVu == null || tenDichVu.trim().isEmpty()) {
            // required đã được xử lý bởi form, bỏ qua
            return;
        }

        String normalizedInput = tenDichVu.trim().toLowerCase();

        // Tìm bản ghi đã tồn tại có cùng tên đã trim bằng JPQL (chính xác, có index nếu có),
        // sau đó lọc thêm ở Java để so sánh không phân biệt hoa/thường (an toàn cho mọi DB dialect).
        List<DmDichVu> candidates = dataManager.load(DmDichVu.class)
                .query("select d from DmDichVu d where d.tenDichVu = :tenDichVu")
                .parameter("tenDichVu", tenDichVu.trim())
                .list();

        DmDichVu existing = candidates.stream()
                .filter(d -> d.getTenDichVu() != null
                        && d.getTenDichVu().trim().toLowerCase().equals(normalizedInput))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            // Khi đang sửa, bỏ qua chính bản ghi hiện tại
            if (entity.getId() == null || !existing.getId().equals(entity.getId())) {
                event.getErrors().add(tenDichVuField,
                        messageBundle.getMessage("validation.dmDichVu.tenDichVuExists"));
            }
        }
    }
}