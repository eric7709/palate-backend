package com.app.palate.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.exceptions.UnauthorizedException;
import com.app.palate.security.JwtUtil;
import com.app.palate.utils.ValidationUtils;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;
    private final JwtUtil jwtUtil;

    // -----------------------------
    // SIGNUP
    // -----------------------------
    public AuthResponseDTO signup(AccountRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getEmail(), "Email");
        ValidationUtils.requireNonBlank(request.getPassword(), "Password");

        checkDuplicate(request);
        Account account = createAccount(request);

        String accessToken = jwtUtil.generateAccessToken(account);
        String refreshToken = jwtUtil.generateRefreshToken(account);
        return new AuthResponseDTO(accessToken, refreshToken, accountMapper.mapToDto(account));
    }

    // -----------------------------
    // CREATE EMPLOYEE (INTERNAL)
    // -----------------------------
    public Account createEmployee(AccountRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getEmail(), "Email");

        checkDuplicate(request);
        return createAccount(request);
    }

    // -----------------------------
    // UPDATE EMPLOYEE
    // -----------------------------
    public Account updateEmployee(Long id, AccountRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Account ID");
        ValidationUtils.requireNonNull(request, "Request body");

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Account not found"));

        if (request.getFirstName() != null) {
            account.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            account.setLastName(request.getLastName().trim());
        }
        if (request.getStatus() != null) {
            account.setStatus(request.getStatus());
        }
        if (request.getGender() != null) {
            account.setGender(request.getGender());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(account.getEmail())) {
            String sanitizedEmail = request.getEmail().trim().toLowerCase();
            if (accountRepository.existsByEmail(sanitizedEmail)) {
                throw new BadRequestException("EMAIL_EXISTS");
            }
            account.setEmail(sanitizedEmail);
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().equals(account.getPhoneNumber())) {
            String sanitizedPhone = request.getPhoneNumber().trim();
            if (accountRepository.existsByPhoneNumber(sanitizedPhone)) {
                throw new BadRequestException("PHONE_EXISTS");
            }
            account.setPhoneNumber(sanitizedPhone);
        }

        if (request.getRole() != null) {
            account.setRole(request.getRole());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return accountRepository.save(account);
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Account not found"));
    }

    // -----------------------------
    // LOGIN
    // -----------------------------
    public AuthResponseDTO login(LoginRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.email(), "Email");
        ValidationUtils.requireNonBlank(request.password(), "Password");

        Account account = accountRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(account);
        String refreshToken = jwtUtil.generateRefreshToken(account);
        return new AuthResponseDTO(accessToken, refreshToken, accountMapper.mapToDto(account));
    }

    // -----------------------------
    // DELETE EMPLOYEE
    // -----------------------------
    public void deleteEmployee(Long id) {
        ValidationUtils.requireNonNull(id, "Account ID");

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Account not found"));

        accountRepository.delete(account);
    }

    // -----------------------------
    // GET ALL USERS
    // -----------------------------

    public Page<Account> getAllEmployees(EmployeeSearchCriteria criteria) {

        // 1. Setup sorting
        Sort sort = criteria.getSortDirection().equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(criteria.getSortBy()).descending()
                : Sort.by(criteria.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        // 2. Build dynamic Specification
        Specification<Account> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String cleanSearch = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), cleanSearch),
                        cb.like(cb.lower(root.get("lastName")), cleanSearch),
                        cb.like(cb.lower(root.get("email")), cleanSearch),
                        cb.like(root.get("phoneNumber"), cleanSearch),
                        cb.like(cb.lower(root.get("gender").as(String.class)), cleanSearch),
                        cb.like(cb.lower(root.get("role").as(String.class)), cleanSearch),
                        cb.like(cb.lower(root.get("status").as(String.class)), cleanSearch),
                        cb.like(root.get("id").as(String.class), cleanSearch),
                        cb.like(root.get("createdAt").as(String.class), cleanSearch),
                        cb.like(root.get("updatedAt").as(String.class), cleanSearch)));
            }

            if (criteria.getRole() != null && !criteria.getRole().isBlank()) {
                predicates.add(cb.equal(root.get("role"), criteria.getRole()));
            }

            if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getGender() != null && !criteria.getGender().isBlank()) {
                predicates.add(cb.equal(root.get("gender"), criteria.getGender()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return accountRepository.findAll(spec, pageable);
    }

    // -----------------------------
    // GET CURRENT USER (ME)
    // -----------------------------
    public MeDTO me(String authHeader) {
        String token = extractAndValidateToken(authHeader);
        String email = jwtUtil.getEmail(token);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Account not found"));

        return new MeDTO(
                account.getFirstName(),
                account.getLastName(),
                account.getPhoneNumber(),
                account.getEmail(),
                account.getGender(),
                account.getId(),
                account.getRole());
    }

    // -----------------------------
    // CHANGE PASSWORD (CURRENT USER)
    // -----------------------------
    public MessageResponse changePassword(String authHeader, ChangePasswordRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.currentPassword(), "Current password");
        ValidationUtils.requireNonBlank(request.newPassword(), "New password");

        String token = extractAndValidateToken(authHeader);
        String email = jwtUtil.getEmail(token);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Account not found"));

        if (!passwordEncoder.matches(request.currentPassword(), account.getPassword())) {
            throw new BadRequestException("INVALID_CURRENT_PASSWORD");
        }
        if (passwordEncoder.matches(request.newPassword(), account.getPassword())) {
            throw new BadRequestException("NEW_PASSWORD_SAME_AS_OLD");
        }

        account.setPassword(passwordEncoder.encode(request.newPassword()));
        accountRepository.save(account);
        return new MessageResponse("Password changed successfully");
    }

    // -----------------------------
    // REFRESH ACCESS TOKEN
    // -----------------------------
    public AccessTokenResponse refresh(RefreshTokenRequest request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.refreshToken(), "Refresh token");

        String refreshToken = request.refreshToken().trim();
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = jwtUtil.getRefreshEmail(refreshToken);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Account not found"));

        String newAccessToken = jwtUtil.generateAccessToken(account);
        return new AccessTokenResponse(newAccessToken);
    }

    // -----------------------------
    // PRIVATE HELPERS
    // -----------------------------
    private String extractAndValidateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        if (!jwtUtil.isValid(token)) {
            throw new UnauthorizedException("Access token expired or invalid");
        }
        return token;
    }

    private void checkDuplicate(AccountRequestDTO request) {
        if (request.getEmail() != null && accountRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("EMAIL_EXISTS");
        }
        if (request.getPhoneNumber() != null
                && accountRepository.existsByPhoneNumber(request.getPhoneNumber().trim())) {
            throw new BadRequestException("PHONE_EXISTS");
        }
    }

    private Account createAccount(AccountRequestDTO request) {
        Account account = new Account();
        account.setEmail(request.getEmail().trim().toLowerCase());
        account.setPassword(passwordEncoder.encode(
                request.getPassword() != null && !request.getPassword().isBlank() ? request.getPassword() : "1111111"));
        account.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
        account.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        account.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        account.setRole(request.getRole());
        account.setStatus(AccountStatus.ACTIVE); // Default to ACTIVE for new accounts
        account.setGender(request.getGender());
        return accountRepository.save(account);
    }
}