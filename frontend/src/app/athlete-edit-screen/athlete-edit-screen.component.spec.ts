import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AthleteEditScreenComponent} from './athlete-edit-screen.component';

describe('AthleteEditScreenComponent', () => {
    let component: AthleteEditScreenComponent;
    let fixture: ComponentFixture<AthleteEditScreenComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [AthleteEditScreenComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AthleteEditScreenComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should be created', () => {
        expect(component).toBeTruthy();
    });
});
