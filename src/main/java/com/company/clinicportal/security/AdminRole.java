package com.company.clinicportal.security;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "AdminRole", code = AdminRole.CODE)
public interface AdminRole {
    String CODE = "admin-role";

    @EntityPolicy(entityName = "*", actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityName = "*", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @ViewPolicy(viewIds = "*")
    @SpecificPolicy(resources = "*")
    @MenuPolicy(menuIds = {"LichHen.list", "LichDieuTri.list", "NhanSu.list", "PhieuChiDinh.list", "ThuThuat.list", "DmDichVu.list", "BenhNhan.list", "SoBenhAn.list",
            "ltcs_CoSoKhamChuaBenhLienThong.list", "ltcs_LienThongDonThuocLog.list",
            "ltcs_LienThongDonThuocOutbox.list",
            "ltcs_DmThuoc.list", "ltcs_Icd10.list", "ltcs_DonThuoc.list", "ltcs_DanhMucImportDialog"})
    void screens();

}