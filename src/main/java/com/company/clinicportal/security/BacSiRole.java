package com.company.clinicportal.security;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Vai trò Bác sĩ: được phép kê đơn thuốc (CRUD trên DonThuoc & entity liên quan),
 * xem danh mục thuốc/ICD-10, xem danh sách bệnh nhân/phiếu điều trị.
 *
 * Bác sĩ KHÔNG có menu admin (User list, log liên thông).
 */
@ResourceRole(name = "BacSiRole", code = BacSiRole.CODE, scope = SecurityScope.UI)
public interface BacSiRole extends UiMinimalRole {

    String CODE = "bac-si-role";

    @EntityPolicy(entityName = "DonThuoc", actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityName = "DonThuoc", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityName = "DonThuocChiTiet", actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityName = "DonThuocChiTiet", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityName = "DonThuocChanDoan", actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityName = "DonThuocChanDoan", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityName = "DonThuocDotDung", actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityName = "DonThuocDotDung", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityName = "ltcs_LienThongDonThuocOutbox", actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityName = "ltcs_LienThongDonThuocOutbox", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityName = "ltcs_LienThongDonThuocLog", actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityName = "ltcs_LienThongDonThuocLog", attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityName = "DmThuoc", actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityName = "Icd10", actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityName = "BenhNhan", actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityName = "PhieuDieuTri", actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityName = "NhanSu", actions = {EntityPolicyAction.READ})
    @ViewPolicy(viewIds = {"ltcs_DonThuoc.list", "ltcs_DonThuoc.detail", "ltcs_DonThuoc.preview", "ltcs_DmThuoc.list", "ltcs_Icd10.list",
            "PhieuDieuTri.detail", "BenhNhan.detail", "BenhNhan.list", "NhanSu.list", "NhanSu.detail",
            "ltcs_LienThongDonThuocOutbox.list", "ltcs_LienThongDonThuocOutbox.detail",
            "ltcs_LienThongSync.dialog"})
    @MenuPolicy(menuIds = {"ltcs_DonThuoc.list", "ltcs_DmThuoc.list", "ltcs_Icd10.list",
            "PhieuDieuTri.detail", "BenhNhan.list"})
    @SpecificPolicy(resources = {"ui.cors", "ui.cors.enabled"})
    void screens();
}
