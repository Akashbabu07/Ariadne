package com.Ariadne.project;

import com.Ariadne.project.dto.*;
import com.Ariadne.project.entity.*;
import com.Ariadne.project.repository.*;
import com.Ariadne.shared.exception.ConflictException;
import com.Ariadne.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final RepositoryRepository repositoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        if (organizationRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("An organization with this name already exists");
        }
        Organization org = Organization.builder().name(request.name()).build();
        org = organizationRepository.save(org);
        return toOrgResponse(org);
    }

    public RepositoryResponse getRepositoryByGitUrl(String gitUrl) {
        Repository repo = repositoryRepository.findByGitUrl(gitUrl)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found for gitUrl: " + gitUrl));
        return toRepoResponse(repo);
    }

    public OrganizationResponse getOrganization(UUID orgId) {
        return toOrgResponse(findOrg(orgId));
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        Organization org = findOrg(request.orgId());
        Project project = Project.builder()
                .organization(org)
                .name(request.name())
                .description(request.description())
                .build();
        project = projectRepository.save(project);
        return toProjectResponse(project);
    }

    public List<ProjectResponse> listProjectsByOrg(UUID orgId) {
        return projectRepository.findByOrganizationId(orgId).stream()
                .map(this::toProjectResponse)
                .toList();
    }

    @Transactional
    public RepositoryResponse registerRepository(RegisterRepositoryRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Repository repo = Repository.builder()
                .project(project)
                .gitUrl(request.gitUrl())
                .defaultBranch(request.defaultBranch() != null ? request.defaultBranch() : "main")
                .syncStatus(SyncStatus.PENDING)
                .build();
        repo = repositoryRepository.save(repo);
        return toRepoResponse(repo);
    }

    public List<RepositoryResponse> listRepositoriesByProject(UUID projectId) {
        return repositoryRepository.findByProjectId(projectId).stream()
                .map(this::toRepoResponse)
                .toList();
    }

    // Real cross-project query, used by scheduler-service to find repos due for
    // re-sync. SYNCING is excluded so an in-progress ingestion isn't re-triggered
    // on top of itself.
    public List<RepositoryResponse> listStaleRepositories(Duration staleThreshold) {
        Instant cutoff = Instant.now().minus(staleThreshold);
        return repositoryRepository
                .findBySyncStatusNotAndLastSyncedAtBeforeOrLastSyncedAtIsNull(SyncStatus.SYNCING, cutoff)
                .stream()
                .map(this::toRepoResponse)
                .toList();
    }

    // Called by ingestion-service after a sync completes. Without this, stale-repo
    // detection would re-trigger the same repositories forever, since lastSyncedAt
    // would never advance.
    @Transactional
    public RepositoryResponse markRepositorySynced(UUID repositoryId, SyncStatus status) {
        Repository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found"));
        repo.setSyncStatus(status);
        repo.setLastSyncedAt(Instant.now());
        repo = repositoryRepository.save(repo);
        return toRepoResponse(repo);
    }

    @Transactional
    public MemberResponse addMember(AddMemberRequest request) {
        Organization org = findOrg(request.orgId());

        if (memberRepository.findByOrganizationIdAndUserId(request.orgId(), request.userId()).isPresent()) {
            throw new ConflictException("User is already a member of this organization");
        }

        Member member = Member.builder()
                .organization(org)
                .userId(request.userId())
                .build();
        member = memberRepository.save(member);
        return toMemberResponse(member);
    }

    public List<MemberResponse> listMembersByOrg(UUID orgId) {
        return memberRepository.findByOrganizationId(orgId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    private Organization findOrg(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    private OrganizationResponse toOrgResponse(Organization org) {
        return new OrganizationResponse(org.getId(), org.getName(), org.getCreatedAt());
    }

    private ProjectResponse toProjectResponse(Project project) {
        return new ProjectResponse(
                project.getId(), project.getOrganization().getId(),
                project.getName(), project.getDescription(), project.getCreatedAt()
        );
    }

    private RepositoryResponse toRepoResponse(Repository repo) {
        return new RepositoryResponse(
                repo.getId(), repo.getProject().getId(), repo.getGitUrl(),
                repo.getDefaultBranch(), repo.getSyncStatus().name(),
                repo.getLastSyncedAt(), repo.getCreatedAt()
        );
    }

    private MemberResponse toMemberResponse(Member member) {
        return new MemberResponse(
                member.getId(), member.getOrganization().getId(),
                member.getUserId(), member.getJoinedAt()
        );
    }

    public RepositoryResponse getRepository(UUID repositoryId) {
        Repository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found"));
        return toRepoResponse(repo);
    }
}
