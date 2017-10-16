import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import { HttpModule } from '@angular/http';

import {AppComponent} from './app.component';
import {AthleteService} from './services/athlete.service';

@NgModule({
    declarations: [
        AppComponent
    ],
    imports: [
        BrowserModule,
        HttpModule
    ],
    providers: [AthleteService],
    bootstrap: [AppComponent]
})
export class AppModule {
}
