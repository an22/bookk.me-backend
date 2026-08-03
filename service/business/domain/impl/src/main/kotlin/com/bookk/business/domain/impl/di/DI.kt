package com.bookk.business.domain.impl.di

import com.bookk.business.domain.api.business.operation.CreateBusiness
import com.bookk.business.domain.api.business.operation.DeleteBusiness
import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.business.domain.api.business.operation.GetDashboardBusiness
import com.bookk.business.domain.api.business.operation.GetUserBusinesses
import com.bookk.business.domain.api.business.operation.UpdateBusiness
import com.bookk.business.domain.api.client.operation.CreateClient
import com.bookk.business.domain.api.client.operation.DeleteClient
import com.bookk.business.domain.api.client.operation.GetClients
import com.bookk.business.domain.api.employee.operation.ApproveEmployeeInvitation
import com.bookk.business.domain.api.employee.operation.CreateEmployeeInvitation
import com.bookk.business.domain.api.service.operation.CreateService
import com.bookk.business.domain.api.service.operation.CreateServiceGroup
import com.bookk.business.domain.api.service.operation.DeleteService
import com.bookk.business.domain.api.service.operation.DeleteServiceGroup
import com.bookk.business.domain.api.service.operation.GetServiceGroups
import com.bookk.business.domain.api.service.operation.GetServices
import com.bookk.business.domain.api.service.operation.IssueQuote
import com.bookk.business.domain.api.service.operation.UpdateService
import com.bookk.business.domain.api.user.operation.SyncUserProfile
import com.bookk.business.domain.impl.event.BusinessEventHandlerImpl
import com.bookk.business.domain.impl.operation.business.CreateBusinessImpl
import com.bookk.business.domain.impl.operation.business.DeleteBusinessImpl
import com.bookk.business.domain.impl.operation.business.GetBusinessByIdImpl
import com.bookk.business.domain.impl.operation.business.GetDashboardBusinessImpl
import com.bookk.business.domain.impl.operation.business.GetUserBusinessesImpl
import com.bookk.business.domain.impl.operation.business.UpdateBusinessImpl
import com.bookk.business.domain.impl.operation.client.CreateClientImpl
import com.bookk.business.domain.impl.operation.client.DeleteClientImpl
import com.bookk.business.domain.impl.operation.client.GetClientsImpl
import com.bookk.business.domain.impl.operation.employee.ApproveEmployeeInvitationImpl
import com.bookk.business.domain.impl.operation.employee.CreateEmployeeInvitationImpl
import com.bookk.business.domain.impl.operation.service.CreateServiceGroupImpl
import com.bookk.business.domain.impl.operation.service.CreateServiceImpl
import com.bookk.business.domain.impl.operation.service.DeleteServiceGroupImpl
import com.bookk.business.domain.impl.operation.service.DeleteServiceImpl
import com.bookk.business.domain.impl.operation.service.GetServiceGroupsImpl
import com.bookk.business.domain.impl.operation.service.GetServicesImpl
import com.bookk.business.domain.impl.operation.service.IssueQuoteImpl
import com.bookk.business.domain.impl.operation.service.UpdateServiceImpl
import com.bookk.business.domain.impl.operation.user.SyncUserProfileImpl
import com.bookk.core.data.eventstreaming.EventHandler
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun businessDomainModule() = module {
    singleOf(::GetBusinessByIdImpl) bind GetBusinessById::class
    singleOf(::CreateBusinessImpl) bind CreateBusiness::class
    singleOf(::DeleteBusinessImpl) bind DeleteBusiness::class
    singleOf(::GetDashboardBusinessImpl) bind GetDashboardBusiness::class
    singleOf(::UpdateBusinessImpl) bind UpdateBusiness::class
    singleOf(::GetUserBusinessesImpl) bind GetUserBusinesses::class
    factoryOf(::BusinessEventHandlerImpl) bind EventHandler::class
    singleOf(::CreateClientImpl) bind CreateClient::class
    singleOf(::GetClientsImpl) bind GetClients::class
    singleOf(::DeleteClientImpl) bind DeleteClient::class
    singleOf(::SyncUserProfileImpl) bind SyncUserProfile::class
    singleOf(::CreateServiceImpl) bind CreateService::class
    singleOf(::DeleteServiceGroupImpl) bind DeleteServiceGroup::class
    singleOf(::CreateServiceGroupImpl) bind CreateServiceGroup::class
    singleOf(::DeleteServiceImpl) bind DeleteService::class
    singleOf(::UpdateServiceImpl) bind UpdateService::class
    singleOf(::GetServicesImpl) bind GetServices::class
    singleOf(::GetServiceGroupsImpl) bind GetServiceGroups::class
    singleOf(::IssueQuoteImpl) bind IssueQuote::class
    singleOf(::CreateEmployeeInvitationImpl) bind CreateEmployeeInvitation::class
    singleOf(::ApproveEmployeeInvitationImpl) bind ApproveEmployeeInvitation::class
}