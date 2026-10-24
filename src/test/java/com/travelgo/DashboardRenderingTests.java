package com.travelgo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardRenderingTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin@travelgo.com", roles = "ADMIN")
    void adminDashboardRenders() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Manage Settings")));
    }

    @Test
    @WithMockUser(username = "admin@travelgo.com", roles = "ADMIN")
    void adminManagementPagesRender() throws Exception {
        for (String path : new String[]{
                "/admin/users", "/admin/staff", "/admin/roles", "/admin/permissions",
                "/admin/settings", "/admin/reports"
        }) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser(username = "officer@travelgo.com", roles = "VISA_OFFICER")
    void visaOfficerDashboardRendersWithBoundActions() throws Exception {
        mockMvc.perform(get("/staff/visa-dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Open Full Dossier Queue")));
    }

    @Test
    @WithMockUser(username = "officer@travelgo.com", roles = "VISA_OFFICER")
    void visaDecisionActionReturnsToSelectedDashboardDossier() throws Exception {
        mockMvc.perform(post("/staff/visas/999999/approve")
                        .param("redirect", "dashboard")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/visa-dashboard?applicationId=999999"));
    }

    @Test
    @WithMockUser(username = "manager@travelgo.com", roles = "PACKAGE_MANAGER")
    void packageManagerDashboardRendersWithLiveControls() throws Exception {
        mockMvc.perform(get("/staff/package-dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Launch Package Editor")));
    }

    @Test
    @WithMockUser(username = "manager@travelgo.com", roles = "PACKAGE_MANAGER")
    void packageManagerWorkflowPagesRender() throws Exception {
        for (String path : new String[]{
                "/staff/packages", "/staff/hotels", "/staff/destinations", "/staff/categories",
                "/staff/bookings", "/staff/inquiries", "/staff/reports"
        }) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser(username = "officer@travelgo.com", roles = "VISA_OFFICER")
    void visaOfficerWorkflowPagesRender() throws Exception {
        for (String path : new String[]{"/staff/visas", "/staff/payments", "/staff/reports"}) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser(username = "customer@travelgo.com", roles = "CUSTOMER")
    void customerDashboardRendersWithRecordSpecificActions() throws Exception {
        mockMvc.perform(get("/customer/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Current Bookings Ledger")));
    }

    @Test
    @WithMockUser(username = "customer@travelgo.com", roles = "CUSTOMER")
    void customerWorkflowPagesRender() throws Exception {
        for (String path : new String[]{"/customer/bookings", "/customer/visas", "/customer/notifications", "/customer/settings"}) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        }
    }
}
