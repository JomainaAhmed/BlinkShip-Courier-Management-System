import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should report isLoggedIn false when no token exists', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should report isLoggedIn true when token exists', () => {
    localStorage.setItem('token', 'sample.jwt.token');
    expect(service.isLoggedIn()).toBeTrue();
  });

  it('should clear token and user on logout', () => {
    localStorage.setItem('token', 'sample.jwt.token');
    localStorage.setItem('role', 'ADMIN');
    service.logout();
    expect(service.getToken()).toBeNull();
    expect(service.getRole()).toBeNull();
  });
});
