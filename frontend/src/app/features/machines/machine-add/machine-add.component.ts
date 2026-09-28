import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MachineService } from '../../../core/services/machine.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-machine-add',
  templateUrl: './machine-add.component.html',
  styleUrls: ['./machine-add.component.scss']
})
export class MachineAddComponent implements OnInit, OnDestroy {
  machineForm: FormGroup;
  isSubmitting: boolean = false;

  private machineChangedSubscription?: Subscription;

  constructor(
    private fb: FormBuilder,
    private machineService: MachineService,
    private router: Router,
    private webSocketService: WebSocketService
  ) {
    this.machineForm = this.fb.group({
      name: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.machineChangedSubscription = this.webSocketService.machineChanged.subscribe(
      (machine: any) => {
        if (machine && machine.id) {
          console.log('WebSocket: Nova mašina je kreirana -', machine);
          this.redirectToMachines();
        }
      }
    );
  }

  ngOnDestroy(): void {
    if (this.machineChangedSubscription) {
      this.machineChangedSubscription.unsubscribe();
    }
    console.log('Machine-add komponenta uništena - WebSocket subscription izbrisana');
  }

  onSubmit(): void {
    if (this.machineForm.valid) {
      this.isSubmitting = true;
      const name = this.machineForm.value.name;

      console.log('Slanje zahteva za kreiranje mašine:', name);
      this.machineService.addMachine(name);

      const timeout = setTimeout(() => {
        console.warn(' WebSocket event nije stigao - fallback redirekcija');
        this.isSubmitting = false;
        this.redirectToMachines();
      }, 10000);

      const subscription = this.webSocketService.machineChanged.subscribe(
        (machine: any) => {
          if (machine && machine.id) {
            clearTimeout(timeout);
            subscription.unsubscribe();
            this.isSubmitting = false;
            console.log('Mašina uspešno kreirana preko WebSocket-a');
            this.redirectToMachines();
          }
        }
      );
    }
  }

  private redirectToMachines(): void {
    alert('Nova mašina je dodata.');
    this.router.navigate(['/machines']);
  }
}
