import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MachineAddComponent } from './machine-add.component';

describe('MachineAddComponent', () => {
  let component: MachineAddComponent;
  let fixture: ComponentFixture<MachineAddComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MachineAddComponent]
    });
    fixture = TestBed.createComponent(MachineAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
