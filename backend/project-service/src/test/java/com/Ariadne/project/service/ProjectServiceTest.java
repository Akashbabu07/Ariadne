package com.Ariadne.project.service;

import com.Ariadne.project.ProjectService;
import com.Ariadne.project.dto.*;
import com.Ariadne.project.entity.*;
import com.Ariadne.project.repository.*;
import com.Ariadne.shared.exception.ConflictException;
import com.Ariadne.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private OrganizationRepository organizationRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private RepositoryRepository repositoryRepository;
    @Mock private MemberRepository memberRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(organizationRepository, projectRepository, repositoryRepository, memberRepository);
    }



    @Test
    void createOrganization_duplicateName_throwsConflict() {
        when(organizationRepository.existsByNameIgnoreCase("Acme")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createOrganization(new CreateOrganizationRequest("Acme")))
                .isInstanceOf(ConflictException.class);

        verify(organizationRepository, never()).save(any());
    }

    @Test
    void createOrganization_newName_savesAndReturnsResponse() {
        when(organizationRepository.existsByNameIgnoreCase("Acme")).thenReturn(false);
        Organization saved = Organization.builder().id(UUID.randomUUID()).name("Acme").build();
        when(organizationRepository.save(any(Organization.class))).thenReturn(saved);

        OrganizationResponse response = projectService.createOrganization(new CreateOrganizationRequest("Acme"));

        assertThat(response.name()).isEqualTo("Acme");
    }

    @Test
    void getOrganization_notFound_throwsResourceNotFound() {
        UUID orgId = UUID.randomUUID();
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getOrganization(orgId))
                .isInstanceOf(ResourceNotFoundException.class);
    }



    @Test
    void registerRepository_defaultBranchOmitted_defaultsToMain() {
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().id(projectId)
                .organization(Organization.builder().id(UUID.randomUUID()).name("Acme").build())
                .name("Ariadne").build();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(repositoryRepository.save(any(Repository.class))).thenAnswer(inv -> inv.getArgument(0));

        RepositoryResponse response = projectService.registerRepository(
                new RegisterRepositoryRequest(projectId, "https://github.com/x/y.git", null));

        assertThat(response.defaultBranch()).isEqualTo("main");
        assertThat(response.syncStatus()).isEqualTo("PENDING");
    }

    @Test
    void registerRepository_explicitBranch_respectsIt() {
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().id(projectId)
                .organization(Organization.builder().id(UUID.randomUUID()).name("Acme").build())
                .name("Ariadne").build();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(repositoryRepository.save(any(Repository.class))).thenAnswer(inv -> inv.getArgument(0));

        RepositoryResponse response = projectService.registerRepository(
                new RegisterRepositoryRequest(projectId, "https://github.com/x/y.git", "develop"));

        assertThat(response.defaultBranch()).isEqualTo("develop");
    }

    @Test
    void registerRepository_projectNotFound_throwsResourceNotFound() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.registerRepository(
                new RegisterRepositoryRequest(projectId, "https://github.com/x/y.git", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getRepositoryByGitUrl_notFound_throwsResourceNotFound() {
        when(repositoryRepository.findByGitUrl("https://unknown.git")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getRepositoryByGitUrl("https://unknown.git"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getRepositoryByGitUrl_found_returnsMappedResponse() {
        Project project = Project.builder().id(UUID.randomUUID())
                .organization(Organization.builder().id(UUID.randomUUID()).name("Acme").build())
                .name("Ariadne").build();
        Repository repo = Repository.builder().id(UUID.randomUUID()).project(project)
                .gitUrl("https://github.com/x/y.git").defaultBranch("main")
                .syncStatus(SyncStatus.SYNCED).build();
        when(repositoryRepository.findByGitUrl("https://github.com/x/y.git")).thenReturn(Optional.of(repo));

        RepositoryResponse response = projectService.getRepositoryByGitUrl("https://github.com/x/y.git");

        assertThat(response.gitUrl()).isEqualTo("https://github.com/x/y.git");
        assertThat(response.syncStatus()).isEqualTo("SYNCED");
    }


    @Test
    void listStaleRepositories_excludesSyncingStatus() {

        projectService.listStaleRepositories(Duration.ofMinutes(60));

        verify(repositoryRepository).findBySyncStatusNotAndLastSyncedAtBeforeOrLastSyncedAtIsNull(
                eq(SyncStatus.SYNCING), any());
    }



    @Test
    void addMember_alreadyMember_throwsConflict() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(Organization.builder().id(orgId).name("Acme").build()));
        when(memberRepository.findByOrganizationIdAndUserId(orgId, userId))
                .thenReturn(Optional.of(Member.builder().id(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> projectService.addMember(new AddMemberRequest(orgId, userId)))
                .isInstanceOf(ConflictException.class);

        verify(memberRepository, never()).save(any());
    }
}