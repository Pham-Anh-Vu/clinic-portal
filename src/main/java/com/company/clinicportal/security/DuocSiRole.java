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
 * Vai trò Dược sĩ: chỉ được XEM đơn thuốc đã kê, không sửa.
 *
 * Hữu ích cho dược sĩ lúc cấp phát thuốc, kiểm tra tương tác, theo dõi bệnh nhân.
 * Dược sĩ KHÔNG có quyền CRUD DonThuoc ở mức entity; chỉ READ.
 */
@ResourceRole(name = "DuocSiRole", code = DuocSiRole.CODE, scope = SecurityScope.UI)
public interface DuocSiRole extends UiMinimalRole {

    String CODE = "duoc-si-role";

    @EntityPolicy(entityName = "DonThuoc", actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityName = "DonThuoc", attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityName = "DonThuocChiTiet", actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityName = "DonThuocChiTiet", attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityName = "DonThuocChanDoan", actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityName = "DonThuocChanDoan", attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityName = "DonThuocDotDung", actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityName = "DonThuocDotDung", attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityName = "ltcs_LienThongDonThuocOutbox", actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityName = "ltcs_LienThongDonThuocOutbox", attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityName = "ltcs_LienThongDonThuocLog", actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityName = "ltcs_LienThongDonThuocLog", attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityName = "DmThuoc", actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityName = "Icd10", actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityName = "BenhNhan", actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityName = "PhieuDieuTri", actions = {EntityPolicyAction.READ})
    @ViewPolicy(viewIds = {"ltcs_DonThuoc.list", "ltcs_DonThuoc.preview", "ltcs_DmThuoc.list", "ltcs_Icd10.list",
            "PhieuDieuTri.detail", "BenhNhan.detail", "BenhNhan.list",
            "ltcs_LienThongDonThuocOutbox.list", "ltcs_LienThongDonThuocOutbox.detail"})
    @MenuPolicy(menuIds = {"ltcs_DonThuoc.list", "ltcs_DmThuoc.list", "ltcs_Icd10.list", "BenhNhan.list"})
    @SpecificPolicy(resources = {"ui.cors", "ui.cors.enabled"})
    void screens();
}
