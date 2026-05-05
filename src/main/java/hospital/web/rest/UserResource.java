package hospital.web.rest;

import hospital.config.Constants;
import hospital.domain.Appointment;
import hospital.domain.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.UserRepository;
import hospital.security.AuthoritiesConstants;
import hospital.service.MailService;
import hospital.service.UserService;
import hospital.service.dto.AdminUserDTO;
import hospital.service.dto.PageResponseDTO;
import hospital.service.dto.PaginationDTO;
import hospital.web.rest.errors.BadRequestAlertException;
import hospital.web.rest.errors.EmailAlreadyUsedException;
import hospital.web.rest.errors.LoginAlreadyUsedException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the {@link hospital.domain.User} entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api/admin")
public class UserResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList(
            "id",
            "login",
            "firstName",
            "lastName",
            "email",
            "activated",
            "langKey",
            "createdBy",
            "createdDate",
            "createdAt",
            "lastModifiedBy",
            "lastModifiedDate"
        )
    );

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserService userService;

    private final UserRepository userRepository;

    private final AppointmentRepository appointmentRepository;

    private final MailService mailService;

    public UserResource(
        UserService userService,
        UserRepository userRepository,
        AppointmentRepository appointmentRepository,
        MailService mailService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.mailService = mailService;
    }

    /**
     * {@code POST  /admin/users}  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends a
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user, or with status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @PostMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<User> createUser(@Valid @RequestBody AdminUserDTO userDTO) throws URISyntaxException {
        LOG.debug("REST request to save User : {}", userDTO);

        if (userDTO.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else if (userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).isPresent()) {
            throw new LoginAlreadyUsedException();
        } else if (userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException();
        } else {
            User newUser = userService.createUser(userDTO);
            mailService.sendCreationEmail(newUser);
            return ResponseEntity.created(new URI("/api/admin/users/" + newUser.getLogin()))
                .headers(
                    HeaderUtil.createAlert(applicationName, "A user is created with identifier " + newUser.getLogin(), newUser.getLogin())
                )
                .body(newUser);
        }
    }

    /**
     * {@code PUT /admin/users} : Updates an existing User.
     *
     * @param userDTO the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already in use.
     */
    @PutMapping({ "/users", "/users/{login}" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> updateUser(
        @PathVariable(name = "login", required = false) @Pattern(regexp = Constants.LOGIN_REGEX) String login,
        @Valid @RequestBody AdminUserDTO userDTO
    ) {
        LOG.debug("REST request to update User : {}", userDTO);
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.orElseThrow().getId().equals(userDTO.getId()))) {
            throw new EmailAlreadyUsedException();
        }
        existingUser = userRepository.findOneByLogin(userDTO.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.orElseThrow().getId().equals(userDTO.getId()))) {
            throw new LoginAlreadyUsedException();
        }
        Optional<AdminUserDTO> updatedUser = userService.updateUser(userDTO);

        return ResponseUtil.wrapOrNotFound(
            updatedUser,
            HeaderUtil.createAlert(applicationName, "A user is updated with identifier " + userDTO.getLogin(), userDTO.getLogin())
        );
    }

    /**
     * {@code GET /admin/users} : get all users with all the details - calling this are only allowed for the administrators.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PageResponseDTO<Map<String, Object>>> getAllUsers(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        LOG.debug("REST request to get all User for an admin");
        List<User> users = userRepository.findAll();
        if (search != null && !search.isBlank()) {
            String like = search.toLowerCase();
            users = users
                .stream()
                .filter(
                    u ->
                        (u.getLogin() != null && u.getLogin().toLowerCase().contains(like)) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(like)) ||
                        (fullName(u).toLowerCase().contains(like)) ||
                        (u.getPhoneNumber() != null && u.getPhoneNumber().toLowerCase().contains(like))
                )
                .toList();
        }
        users = users.stream().sorted(sorter(sortBy, sortOrder)).toList();
        List<User> paged = slice(users, page, limit);
        List<Map<String, Object>> data = paged.stream().map(this::toSummary).toList();
        return ResponseEntity.ok(
            new PageResponseDTO<>(data, new PaginationDTO(page, limit, users.size(), totalPages(users.size(), limit)))
        );
    }

    /**
     * {@code GET /admin/users/:login} : get the "login" user.
     *
     * @param login the login of the user to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" user, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/users/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> getUser(@PathVariable("login") @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        LOG.debug("REST request to get User : {}", login);
        return ResponseUtil.wrapOrNotFound(userService.getUserWithAuthoritiesByLogin(login).map(AdminUserDTO::new));
    }

    /**
     * {@code DELETE /admin/users/:login} : delete the "login" User.
     *
     * @param login the login of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable("id") String id) {
        LOG.debug("REST request to delete User: {}", id);
        Optional<User> user = userRepository.findOneByLogin(id);
        if (user.isPresent()) {
            userRepository.delete(user.orElseThrow());
        } else if (id.matches("\\d+")) {
            userRepository.findById(Long.parseLong(id)).ifPresent(userRepository::delete);
        } else {
            userService.deleteUser(id);
        }
        return ResponseEntity.ok(Map.of("id", id, "message", "Người dùng đã được xóa"));
    }

    private Map<String, Object> toSummary(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        map.put(
            "fullName",
            ((user.getFirstName() == null ? "" : user.getFirstName()) + " " + (user.getLastName() == null ? "" : user.getLastName())).trim()
        );
        map.put("phoneNumber", user.getPhoneNumber());
        map.put("createdAt", user.getCreatedDate());
        map.put(
            "appointments",
            appointmentRepository.findAll().stream().filter(a -> a.getUser() != null && a.getUser().getId().equals(user.getId())).count()
        );
        return map;
    }

    private List<User> slice(List<User> items, int page, int limit) {
        int fromIndex = Math.min(Math.max(page - 1, 0) * limit, items.size());
        int toIndex = Math.min(fromIndex + limit, items.size());
        return fromIndex >= toIndex ? List.of() : items.subList(fromIndex, toIndex);
    }

    private int totalPages(int total, int limit) {
        if (limit <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) total / (double) limit);
    }

    private Comparator<User> sorter(String sortBy, String sortOrder) {
        Comparator<User> comparator;
        if ("login".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(u -> Optional.ofNullable(u.getLogin()).orElse(""));
        } else if ("email".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(u -> Optional.ofNullable(u.getEmail()).orElse(""));
        } else if ("firstName".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(u -> Optional.ofNullable(u.getFirstName()).orElse(""));
        } else if ("lastName".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(u -> Optional.ofNullable(u.getLastName()).orElse(""));
        } else {
            comparator = Comparator.comparing(u -> Optional.ofNullable(u.getCreatedDate()).orElse(Instant.EPOCH));
        }
        return "asc".equalsIgnoreCase(sortOrder) ? comparator : comparator.reversed();
    }

    private String fullName(User user) {
        return (
            (user.getFirstName() == null ? "" : user.getFirstName()) + " " + (user.getLastName() == null ? "" : user.getLastName())
        ).trim();
    }
}
